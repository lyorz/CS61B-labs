package gitlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class myUtils {

    /* Commit、Blobs对象读取 */

    /**
     * 读取头提交
     * @return      返回头提交对应的Commit对象
     */
    public static Commit readHeadCommit() {
        File headCommitFile = Utils.readObject(Repository.HEAD, File.class);
        String headCommitID = Utils.readContentsAsString(headCommitFile);
        return Commit.readCommitWithID(headCommitID);
    }

    /**
     * 读取当前头提交所追踪的文件树
     * @return      返回头提交追踪文件
     */
    public static TreeMap<String, String> readHeadTrackingTree() {
        Commit head = readHeadCommit();
        return head.getTrackingTree().getAddedFiles();
    }

    /**
     * 读取给定提交追踪文件树
     * @param c     给定提交
     * @return      给定提交的追踪文件树
     */
    public static TreeMap<String, String> readCommitTrackingTree(Commit c) {
        return c.getTrackingTree().getAddedFiles();
    }

    /**
     * 寻找给定提交的父提交，保证父提交和给定提交位于同一分支。
     * @param c             给定提交
     * @param branchName    给定提交所处分支
     * @return              给定提交的父提交
     */
    public static Commit getParentCommit(Commit c, String branchName) {
        Commit firstParent = c.getParent();
        // 如果当前提交为merge的结果
        if (c.isMergedCommit()) {
            // 读取第二父提交
            Commit secondParent = c.getSecondParent();
            // 且给定分支和第二父提交相同，则返回第二父提交
            if (branchName.equals(secondParent.getBranch())) {
                return secondParent;
            }
        }
        // 否则返回第一父提交
        return firstParent;
    }

    /**
     * 读取当前所处分支名
     * @return  分支名
     */
    public static String readCurrBranchName() {
        return Utils.readObject(Repository.HEAD, File.class).getName();
    }

    /**
     * 读取暂存区
     * @return 暂存区对应的Blobs对象
     */
    public static Blobs readStagingArea() {
        return new Blobs(Repository.INDEX);
    }
    
    /**
     * 将暂存区内容保存
     * @param stagingArea   表明暂存区状态的Blobs对象
     */
    public static void saveStagingArea(Blobs stagingArea) {
        stagingArea.saveBlobs(Repository.INDEX);
    }

    /**
     * 清空暂存区内容
     */
    public static void clearStagingArea() {
        // 清空暂存区
        try {
            FileWriter writer = new FileWriter(Repository.INDEX);
            writer.write("");
            writer.flush();
            writer.close();
        } catch (IOException ignore) {  }
    }

    /* 文件读写 */

    /**
     * 读取工作目录下文件
     * @return  返回工作目录下文件列表
     */
    public static List<String> readCWDFiles() {
        return Utils.plainFilenamesIn(Repository.CWD);
    }

    /**
     * 根据文件ID读取其中内容
     * @param fileID    给定文件ID
     * @return          返回String类型的文件内容
     */
    public static String readContentWithFileID(String fileID) {
        if (fileID == null) {
            return "";
        }
        File fileDir = Utils.join(Repository.OBJECTS_DIR, fileID.substring(0, 2));
        File file = Utils.join(fileDir, fileID.substring(2));
        return Utils.readContentsAsString(file);
    }

    /**
     * 将冲突内容写入冲突文件
     * @param filename      冲突文件名
     * @param currSha1      当前文件的内容ID
     * @param givenSha1     给定文件内容ID
     */
    private static void writeConflict(String filename, String currSha1, String givenSha1) {
        // 读取暂存区
        Blobs stagingArea = myUtils.readStagingArea();
        String currContent = readContentWithFileID(currSha1);
        String givenContent = readContentWithFileID(givenSha1);
        String conflictMessage = conflictMessage(currContent, givenContent);
        // 重写冲突文件
        overrideFileWithContent(conflictMessage, filename);
        // 将冲突文件加入add暂存区
        stagingArea.operateStagingArea(filename, "add");
        // 保存暂存区
        myUtils.saveStagingArea(stagingArea);
    }

    /**
     * 根据给定的内容文件ID重写文件
     * @param fileID        指定文件内容存储ID
     * @param filename      需要重写的文件名
     */
    public static void overrideFile(String fileID, String filename) {
        // 读取文件内容
        String strContent = readContentWithFileID(fileID);
        // 如果文件存在于当前目录，覆盖
        File f = new File(filename);
        if (f.exists()) {
            try {
                FileWriter writer = new FileWriter(filename);
                writer.write(strContent);
                writer.close();
            }
            catch (IOException ignore) {}
        } else {
            // 否则新建并写入
            Utils.writeContents(f, strContent);
        }
    }

    /**
     * 根据给定的内容重写文件
     * @param content       指定文件内容
     * @param filename      需要重写的文件名
     */
    public static void overrideFileWithContent(String content, String filename) {
        // 如果文件存在于当前目录，覆盖
        File f = new File(filename);
        if (f.exists()) {
            try {
                FileWriter writer = new FileWriter(filename);
                writer.write(content);
                writer.close();
            }
            catch (IOException ignore) {}
        } else {
            // 否则新建并写入
            Utils.writeContents(f, content);
        }
    }

    /**
     * 将工作目录下文件恢复到目标文件追踪树中的状态
     * @param trackingTree  需要恢复的目标文件追踪树
     */
    public static void resetCWDFiles(TreeMap<String, String> trackingTree) {
        List<String> files = readCWDFiles();
        // 删除工作目录所有文件
        for (String cwdFilename : files) {
            Utils.deleteFile(cwdFilename);
        }
        // 重写签出所跟踪的所有文件
        for (Map.Entry<String, String> entry : trackingTree.entrySet()) {
            overrideFile(entry.getValue(), entry.getKey());
        }
        // 清空暂存区
        clearStagingArea();
    }

    /**
     * 检查短ID（长度小于40的sha1值）和文件名是否匹配
     * @param ID            shortID，指长度小于40的sha1值
     * @param filename      要检查的文件名
     * @return              返回短ID和文件名是否匹配
     */
    public static boolean checkShortID(String ID, String filename) {
        // 将ID缩减两位
        String subID = ID.substring(2);
        // 将filename缩减为和短ID一样长
        String shortFilename = filename.substring(0, subID.length());
        return subID.equals(shortFilename);
    }

    /* 当前工作目录下文件相对于头提交的状态 */

    /**
     * 打印状态信息
     * @param title     状态栏标题
     * @param objects   状态内容物
     */
    public static void printStatus(String title, ArrayList<String> objects) {
        System.out.println(title);
        for (String object : objects) {
            System.out.println(object);
        }
        System.out.println();
    }

    /**
     * 获取当前分支状态
     */
    public static void branchStatus() {
        // 读取所有分支文件
        List<String> branchFilenames = Utils.plainFilenamesIn(Repository.HEADS_DIR);
        // 获取当前所处分支
        String currHeadName = readCurrBranchName();
        System.out.println("=== Branches ===\n*" + currHeadName);
        if (branchFilenames != null) {
            for (String branchFilename : branchFilenames) {
                if (branchFilename.equals(currHeadName)) {
                    continue;
                }
                System.out.println(branchFilename);
            }
        }
        System.out.println();
    }

    /**
     * add暂存区状态
     */
    public static void addStageStatus() {
        Blobs stagingArea = readStagingArea();
        // 读取add暂存区文件
        TreeMap<String, String> addStagingArea = stagingArea.getAddedFiles();
        ArrayList<String> addFilenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : addStagingArea.entrySet()) {
            addFilenames.add(entry.getKey());
        }
        printStatus("=== Staged Files ===", addFilenames);
    }

    /**
     * rm暂存区状态
     */
    public static void rmStageStatus() {
        Blobs stagingArea = readStagingArea();
        // 读取删除暂存区文件
        TreeMap<String, String> rmStagingArea = stagingArea.getRemovedFiles();
        ArrayList<String> rmFilenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : rmStagingArea.entrySet()) {
            rmFilenames.add(entry.getKey());
        }
        printStatus("=== Removed Files ===", rmFilenames);
    }

    /**
     * 工作目录下已修改未暂存的文件
     * @return  返回工作目录中未暂存的修改列表
     */
    public static ArrayList<String> notStagedStatus() {
        // 读取暂存区
        Blobs stagingArea = readStagingArea();
        // 读取头提交追踪树
        TreeMap<String, String> currHeadTrackingTree = readHeadTrackingTree();
        // 读取工作区文件目录
        List<String> cwdFiles = readCWDFiles();
        // 记录文件状态：修改/删除未暂存
        ArrayList<String> notStaged = new ArrayList<>();

        // 遍历头提交文件
        for (Map.Entry<String, String> entry : currHeadTrackingTree.entrySet()) {
            // 如果文件存在于暂存区，跳过
            String[] findRes = stagingArea.find(entry.getKey());
            if (findRes[0] != null || findRes[1] != null) {
                continue;
            }
            String curr_filename = entry.getKey();
            // 如果文件存在于工作目录
            if (cwdFiles.contains(entry.getKey())) {
                // 计算工作目录下文件的sha-1哈希值
                File f = new File(curr_filename);
                String curr_file_sha1 = Utils.sha1(Utils.readContentsAsString(f));
                // 如果结果和追踪树中记录的不等，则存在未暂存的修改
                if (!curr_file_sha1.equals(entry.getValue())) {
                    notStaged.add(curr_filename + " (modified)");
                }
            } else {
                // 如果文件不存在
                // 则文件被删除且未加入暂存区
                notStaged.add(curr_filename + " (deleted)");
            }
        }

        return notStaged;
    }

    /**
     * 获取当前工作目录下未追踪状态的文件
     * @return      返回未追踪文件列表
     */
    public static ArrayList<String> untrackedStatus() {
        // 读取暂存区
        Blobs stagingArea = readStagingArea();
        // 读取工作目录下文件列表
        List<String> cwdFiles = readCWDFiles();
        // 读取当前头提交所追踪文件
        TreeMap<String, String> currHeadTrackingTree = readHeadTrackingTree();
        // 存在于工作目录且不存在于追踪树的文件
        ArrayList<String> untracked = new ArrayList<>();

        // 如果工作区文件目录为空，返回null
        if (cwdFiles.isEmpty()) {
            return untracked;
        }

        for (String curr_filename : cwdFiles) {
            // 如果文件存在于暂存区，跳过
            String[] findRes = stagingArea.find(curr_filename);
            if (findRes[0] != null || findRes[1] != null) {
                continue;
            }
            String trackedSha1 = currHeadTrackingTree.get(curr_filename);
            // 新增文件，直接保存
            if (trackedSha1 == null) {
                untracked.add(curr_filename);
            }
        }
        return untracked;
    }

    /* merge相关 */

    /**
     * 寻找分割点
     * @param currHead      当前分支头提交
     * @param givenHead     给定分支头提交
     * @return              分割点提交
     */
    public static Commit findSplitPoint(Commit currHead, Commit givenHead) {
        // 读取当前头提交到提交图中其他所有节点的距离
        TreeMap<String, Integer> currHeadDisTable = Commit.calDistance(currHead);
        // 读取给定头提交到提交图中其他所有节点的距离
        TreeMap<String, Integer> givenHeadDisTable = Commit.calDistance(givenHead);

        String nearestNodeID = null;
        int minDistance = 2 * currHeadDisTable.size() + 1;

        for (Map.Entry<String, Integer> e : currHeadDisTable.entrySet()) {
            int currDis = e.getValue();
            int givenDis = givenHeadDisTable.get(e.getKey());

            if (currDis == -1 || givenDis == -1) {
                continue;
            }

            if (currDis + givenDis < minDistance) {
                minDistance = currDis + givenDis;
                nearestNodeID = e.getKey();
            }
        }
        return Commit.readCommitWithID(nearestNodeID);
    }


    /**
     * 返回分割点和给定提交追踪文件的比较结果
     * @param splitPoint    分割点提交
     * @param c             给定提交
     * @return 返回文件比较结果：0-修改，1-删除，2-保持相同, 3-新增
     */
    public static ArrayList<TreeMap<String, String>> compareFiles(Commit splitPoint, Commit c) {
        TreeMap<String, String> splitPointTracking = readCommitTrackingTree(splitPoint);
        TreeMap<String, String> branchTracking = readCommitTrackingTree(c);
        ArrayList<TreeMap<String, String>> compareRes = new ArrayList<>();

        // 初始化ArrayList中的每个TreeMap
        for (int i = 0; i < 4; i++) {
            TreeMap<String, String> treeMap = new TreeMap<>();
            // 添加元素到TreeMap
            compareRes.add(treeMap);
        }
        // 遍历分割点存储条目
        for (Map.Entry<String, String> entry : splitPointTracking.entrySet()) {
            // 在头提交中查找分割点存储文件
            String findRes = branchTracking.get(entry.getKey());
            int putIndex;
            // 如果在头提交追踪文件中没找到，则文件被删除
            if (findRes == null) {
                putIndex = 1;
            } else {
                // 否则从提交树中删除该文件
                branchTracking.remove(entry.getKey());
                // 比较头提交和分割点中的文件内容，如果相同则未作改变
                if (findRes.equals(entry.getValue())) {
                    putIndex = 2;
                } else {
                    // 否则文件被覆盖了
                    putIndex = 0;
                }
            }
            // 将文件状态写入对应的索引区
            compareRes.get(putIndex).put(entry.getKey(), findRes);
        }
        compareRes.set(3, branchTracking);
        return compareRes;
    }

    /**
     * 生成conflict信息
     * @param currContent       当前文件内容
     * @param givenContent      给定文件内容
     * @return                  生成的冲突文件内容
     */
    private static String conflictMessage(String currContent, String givenContent) {
        return "<<<<<<< HEAD\n" +
                currContent + "=======\n"
                + givenContent + ">>>>>>>\n";
    }

    /**
     * 寻找当前分支和给定分支中相对于它们的公共祖先，修改冲突的部分：
     * 1. 在一个分支删除而另一个没删除且更改；
     * 2. 在两个分支都修改，但内容不一；
     * 3. 在两个分支都新增，但内容不一。
     * @param splitPoint            分割点
     * @param givenBranchHead       给定分支头提交
     * @return key:filename-value[file content in curr branch,file content in given branch]
     */
    public static boolean solveConflict(Commit splitPoint, Commit givenBranchHead) {
        // 读取当前分支头提交
        Commit head = readHeadCommit();
        // 获取当前分支头提交和分割点的比较结果
        ArrayList<TreeMap<String, String>> currBranchCmpRes = compareFiles(splitPoint, head);
        // 获取给定分支头提交和分割点的比较结果
        ArrayList<TreeMap<String, String>> branchCmpRes = compareFiles(splitPoint, givenBranchHead);

        boolean conflictExists = false;
        // 在当前分支删除，给定分支更改
        for (Map.Entry<String, String> e : currBranchCmpRes.get(1).entrySet()) {
            String findRes = branchCmpRes.get(0).get(e.getKey());
            if (findRes != null) {
                writeConflict(e.getKey(), null, findRes);
                conflictExists = true;
            }
        }
        // 在给定分支删除，在当前分支修改
        for (Map.Entry<String, String> e : branchCmpRes.get(1).entrySet()) {
            String findRes = currBranchCmpRes.get(0).get(e.getKey());
            if (findRes != null) {
                writeConflict(e.getKey(), findRes, e.getValue());
                conflictExists = true;
            }
        }
        // 在双分支都修改但内容不一
        for (Map.Entry<String, String> e : currBranchCmpRes.get(0).entrySet()) {
            String findRes = branchCmpRes.get(0).get(e.getKey());
            if (findRes != null && !findRes.equals(e.getValue())) {
                writeConflict(e.getKey(), e.getValue(), findRes);
                conflictExists = true;
            }
        }
        // 在双分支都新增但内容不一
        for (Map.Entry<String, String> e : currBranchCmpRes.get(3).entrySet()) {
            String findRes = branchCmpRes.get(3).get(e.getKey());
            if (findRes != null && !findRes.equals(e.getValue())) {
                writeConflict(e.getKey(), e.getValue(), findRes);
                conflictExists = true;
            }
        }
        return conflictExists;
    }

}
