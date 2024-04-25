package gitlet;

import java.io.*;
import java.util.*;

public class Command {
    private static final File INDEX = Utils.join(".gitlet", "index");
    static final File HEAD = Utils.join(".gitlet", "HEAD");
    static final File OBJECTS_DIR = Utils.join(".gitlet", "objects");
    static final File HEADS_DIR = Utils.join(".gitlet", "refs", "heads");

    /** 辅助函数：读取头提交
     * @return c
     */
    private static Commit readHeadCommit() {
        File headCommitFile = Utils.readObject(HEAD, File.class);
        String headCommitID = Utils.readContentsAsString(headCommitFile);
        return Commit.fromfile(Utils.join(OBJECTS_DIR, headCommitID.substring(0,2), headCommitID.substring(2)));
    }


    /** 辅助函数：根据输入读取对应ID的Commit对象
     * @return c
     */
    private static Commit readCommitWithID(String commitID) {
        File commitFile = Utils.join(OBJECTS_DIR, commitID.substring(0,2), commitID.substring(2));
        // 如果存在这样的提交
        if (commitFile.exists()) {
            return Commit.fromfile(commitFile);
        }
        return null;
    }

    /** 辅助函数：查找给定名称的分支是否存在
     */
    private static boolean branchExists(String branchName) {
        // 读取所有branch
        List<String> branchNameList = Utils.plainFilenamesIn(HEADS_DIR);

        if (branchNameList != null) {
            for (String b : branchNameList) {
                if (b.equals(branchName)) {
                    return true;
                }
            }
        }

        return false;
    }
    /**DESCRIPTION -- init
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit:
     * a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
     * It will have a single branch: master, which initially points to this initial commit, and master will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates
     * (this is called “The (Unix) Epoch”, represented internally by the time 0.)
     * Since the initial commit in all repositories created by Gitlet will have exactly the same content,
     * it follows that all repositories will automatically share this commit (they will all have the same UID)
     * and all commits in all repositories will trace back to it.
     */
    public static void init() {
        if (Repository.GITLET_DIR.exists()) {
            Utils.exitWithError("A Gitlet version-control system already exists in the current directory.");
        }

        // 初始化.gitlet/
        if (Repository.setupPersistence()) {
            // 创建初始提交
            Commit c = new Commit("initial commit");
            // 记录当前commit
            c.saveCommit();
            return;
        }
        // 已存在.gitlet则推出
        Utils.exitWithError("A Gitlet version-control system already exists in the current directory.");
    }

    /**DESCRIPTION -- add
     * Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
     * For this reason, adding a file is also called staging the file for addition.
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area if it is already there
     * (as can happen when a file is changed, added, and then changed back to it’s original version).
     * The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
     */
    public static void add(String addFile) {
        // 读取暂存区
        Blobs b = new Blobs(INDEX);
        // 获取头提交
        Commit head = readHeadCommit();
        // 获取头提交追踪的Blobs对象
        Blobs headTrackingTree = head.getTrackingTree();
        // 文件对象
        File f = new File(addFile);

        if (!f.exists()) {
            Utils.exitWithError("File does not exist.");
        }

        // 查找add的文件是否在rm暂存区中
        String[] findRes = b.find(addFile);
        // 如果在rm暂存区
        if (findRes[1] != null) {
            // 则取消删除并退出
            b.remove(addFile);
            // 保存暂存区状态
            b.saveBlobs(INDEX);
            System.exit(0);
        }

        // 查找add的文件是否已追踪
        String[] headRes = headTrackingTree.find(addFile);
        // 如果已追踪
        if (headRes[0] != null) {
            // 计算当前文件哈希值
            String newContentSha1 = Utils.sha1(Utils.readContentsAsString(f));
            // 获取追踪树哈希表
            TreeMap<String, String> trackingHash = headTrackingTree.getAddedFiles();
            String trackedSha1 = trackingHash.get(addFile);
            // 判断文件内容是否和追踪记录相等
            if (newContentSha1.equals(trackedSha1)) {
                Utils.exitWithError("");
            }
        }

        // 加入add暂存区
        b.operateStagingArea(addFile, "add");
        // 保存暂存区状态
        b.saveBlobs(INDEX);
    }

    /** 辅助函数：清空暂存区
     *
     */
    private static void clearStagingArea() {
        // 清空暂存区
        try{
            FileWriter writer = new FileWriter(INDEX);
            writer.write("");
            writer.flush();
            writer.close();
        }
        catch (IOException ignore) {}
    }
    /**DESCRIPTION -- commit
     * Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
     * The commit is said to be tracking the saved files.
     * By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files;
     * it will keep versions of files exactly as they are, and not update them.
     * A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit,
     * in which case the commit will now include the version of the file that was staged instead of the version it got from its parent.
     * A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     * Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
     */
    public static void commit(String message) {
        // 提交信息为空，报错并退出
        if (message.isEmpty()) {
            Utils.exitWithError("Please enter a commit message.");
        }
        // 读取暂存区
        Blobs b = new Blobs(INDEX);
        // 暂存区没有追踪文件，报错并退出
        if (b.isEmpty()) {
            Utils.exitWithError("No changes added to the commit.");
        }
        // 读取头提交
        Commit c = new Commit(message);
        c.saveCommit();
        clearStagingArea();
    }

    /** DESCRIPTION -- log
     * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents found in merge commits.
     * (In regular Git, this is what you get with git log --first-parent).
     * This set of commit nodes is called the commit’s history.
     * For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.
     * */
    public static void log() {
        // 读取当前分支名
        String branchName = Utils.readObject(HEAD, File.class).getName();
        // 读取头提交
        Commit head = readHeadCommit();
        // 读取父提交
        Commit parent = getParentCommit(head, branchName);

        // 打印头提交
        System.out.println(head.getLog());
        // 从头提交向前遍历
        while (parent != null) {
            System.out.println(parent.getLog());
            head = parent;
            parent = getParentCommit(head, branchName);
        }

    }

    /** DESCRIPTION -- rm
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     */
    public static void rm(String rmfilename) {
        // 读取当前Blogs中暂存文件
        Blobs b = new Blobs(INDEX);
        // 读取头提交追踪树
        Blobs headTrackingTree = readHeadCommit().getTrackingTree();

        // 查找用户删除文件在暂存区
        String[] findRes = b.find(rmfilename);
        if (!(findRes[0] == null && findRes[1] == null)) {
            // 从暂存区中取消
            b.remove(rmfilename);
            // 重新保存Blobs对象
            b.saveBlobs(INDEX);
        }
        // 否则查找文件是否被提交所跟踪
        else {
            String[] headTrackingRes = headTrackingTree.find(rmfilename);

            // 如果被当前提交跟踪
            if (headTrackingRes[0] != null) {
                // 将其加入删除暂存区
                b.operateStagingArea(rmfilename, "rm");
                b.saveBlobs(INDEX);
                // 删除工作目录下的文件
                File rmFile = new File(rmfilename);
                if (rmFile.exists()) {
                    Utils.restrictedDelete(rmFile);
                }
            }
            else {
               Utils.exitWithError("No reason to remove the file.");
            }
        }

    }

    /** DESCRIPTION -- global-log
     *  Like log, except displays information about all commits ever made.
     *  The order of the commits does not matter.
     *  Hint: there is a useful method in gitlet.Utils that will help you iterate over files within a directory.
     * */
    public static void globalLog() {
        // 获取.gitlet/objects/下所有目录名
        String[] dir_list = Utils.DirnamesIn(OBJECTS_DIR);
        // 遍历目录列表
        for(String dir : dir_list) {
            // 读取.gitlet/objects/dir下所有文件
            List<String> filename_list = Utils.plainFilenamesIn(Utils.join(OBJECTS_DIR, dir));
            if (filename_list == null) {continue;}
            // 遍历文件
            for (String filename : filename_list) {
                File f = Utils.join(OBJECTS_DIR, dir, filename);
                // 使用异常捕获，当文件记录Commit对象时，打印log信息
                try {
                    Commit c = Utils.readObject(f, Commit.class);
                    System.out.println(c.getLog());
                }
                // 否则不做处理
                catch (IllegalArgumentException ignore) { }
            }
        }
    }

    /**
     *  Prints out the ids of all commits that have the given commit message, one per line.
     *  If there are multiple such commits, it prints the ids out on separate lines.
     *  The commit message is a single operand; to indicate a multiword message,
     *  put the operand in quotation marks, as for the command below.
     * @param message: 所寻找提交的信息
     */
    public static void find(String message) {
        // 获取.gitlet/objects/下所有目录名
        String[] dir_list = Utils.DirnamesIn(OBJECTS_DIR);
        // 标志是否存在message等于输入参数的提交
        boolean flag = false;
        // 遍历目录列表
        for(String dir : dir_list) {
            // 读取.gitlet/objects/dir下所有文件
            List<String> filename_list = Utils.plainFilenamesIn(Utils.join(OBJECTS_DIR, dir));
            if (filename_list == null) {continue;}
            // 遍历文件
            for (String filename : filename_list) {
                File f = Utils.join(OBJECTS_DIR, dir, filename);
                // 使用异常捕获，当文件记录Commit对象时，比较其message是否和输入参数相同
                try {
                    Commit c = Utils.readObject(f, Commit.class);
                    String id = c.sameMessage(message);
                    if (id != null) {
                        System.out.println(id);
                        flag = true;
                    }
                }
                // 否则不做处理
                catch (IllegalArgumentException ignored) { }
            }
        }
        // 如果flag仍为false，输入出错误消息
        if (!flag) {
            Utils.exitWithError("Found no commit with that message.");
        }
    }

    /** checkout辅助函数，文件重写 */
    private static void overrideFile(String fileID, String filename) {
        // 读取文件内容
        File filecontent = Utils.join(OBJECTS_DIR, fileID.substring(0, 2), fileID.substring(2));
        String strcontent = Utils.readContentsAsString(filecontent);
        // 如果文件存在于当前目录，覆盖
        File f = new File(filename);
        if (f.exists()) {
            try {
                FileWriter writer = new FileWriter(filename);
                writer.write(strcontent);
                writer.close();
            }
            catch (IOException ignore) {}
        }
        else {
            // 否则新建并写入
            Utils.writeContents(f, strcontent);
        }

    }

    /** DESCRIPTION -- checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * */
    public static void checkoutResetfile(String filename) {
        // 获取头提交
        Commit head = readHeadCommit();
        // 获取头提交追踪的Blobs对象
        Blobs b = head.getTrackingTree();
        String fileID = b.find(filename)[0];
        // 如果查找文件不为空
        if (fileID != null) {
            overrideFile(fileID, filename);
        }
        // 抛出异常
        else {
            Utils.exitWithError("File does not exist in that commit.");
        }
    }

    /**
     * 检查短ID（长度小于40的sha1值）和文件名是否匹配
     * @param ID            shortID，指长度小于40的sha1值
     * @param filename      要检查的文件名
     * @return              返回短ID和文件名是否匹配
     */
    private static boolean checkShortID(String ID, String filename) {
        // 将ID缩减两位
        String subID = ID.substring(2);
        // 将filename缩减为和短ID一样长
        String shortFilename = filename.substring(0, subID.length());
        return subID.equals(shortFilename);
    }

    /** DESCRIPTION -- checkout [commit id] -- [file name]
     *  Takes the version of the file as it exists in the commit with the given id,
     *  and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     */
    public static void checkoutLastfile(String ID, String filename) {
        Commit c = null;
        // 获取对应ID的提交
        if (ID.length() < 40) {
            // 存储目录一定是.gitlet/objects/ID[:2]
            File commitDir = Utils.join(OBJECTS_DIR, ID.substring(0,2));
            // 列举当前目录下所有文件
            List<String> filenames = Utils.plainFilenamesIn(commitDir);
            // 查找匹配文件
            for (String f : filenames) {
                if (checkShortID(ID, f)) {
                    String totalID = ID.substring(0,2) + f;
                    c = readCommitWithID(totalID);
                }
            }
        }
        else {
            c = readCommitWithID(ID);
        }


        // 如果不存在这样的提交，抛出异常并退出
        if (c == null) {
            Utils.exitWithError("No commit with that id exists.");
        }

        // 获取提交追踪文件树
        Blobs b = null;
        if (c != null) {
            b = c.getTrackingTree();
        }
        // 查找给定提交中追踪的文件
        String fileID = null;
        if (b != null) {
            fileID = b.find(filename)[0];
        }
        //
        if (fileID == null) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        if (fileID != null) {
            overrideFile(fileID, filename);
        }
    }

    /**
     * checkout辅助函数：使用目标提交追踪文件覆盖当前文件
     * @param dstTrackingTree       目标提交追踪文件
     */
    private static void resetCWDfiles (List<String> cwdFilenames, TreeMap<String, String> dstTrackingTree) {
        // 删除工作目录所有文件
        for (String cwdFilename : cwdFilenames) {
            Utils.deleteFile(cwdFilename);
        }
        // 重写签出所跟踪的所有文件
        for (Map.Entry<String, String> entry : dstTrackingTree.entrySet()) {
            overrideFile(entry.getValue(), entry.getKey());
        }
        // 清空暂存区
        clearStagingArea();
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be considered the current branch
     * (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out
     * branch are deleted. The staging area is cleared,
     * unless the checked-out branch is the current branch
     */
    public static void checkout(String branchName) {
        // 当前头提交文件
        File currheadfile = Utils.readObject(HEAD, File.class);
        // 读取头提交
        Commit currHead = readHeadCommit();

        // 要切换的分支不存在，退出
        if (!branchExists(branchName)) {
            Utils.exitWithError("No such branch exists.");
        }
        // 如果头提交位于branchName分支，无需切换，退出
        if (branchName.equals(currheadfile.getName())) {
            Utils.exitWithError("No need to checkout the current branch.");
        }

        // 读取branchName对应的头提交
        File branchheadfile = Utils.join(HEADS_DIR, branchName);
        String CommitID = Utils.readContentsAsString(branchheadfile);
        Commit branchHead = readCommitWithID(CommitID);
        // 读取切换的branch头提交对应追踪文件
        TreeMap<String, String> branchTrackingTree = null;
        if (branchHead != null) {
            branchTrackingTree = branchHead.getTrackingTree().getAddedFiles();
        }
        // 读取当前头提交追踪文件
        TreeMap<String, String> currBranchTrackingTree = currHead.getTrackingTree().getAddedFiles();

        // 读取暂存区
        Blobs stagingArea = new Blobs(INDEX);
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);
        // 获取工作目录下未被当前提交追踪的文件
        ArrayList<String> untracked = untrackedStatus(
                stagingArea, currBranchTrackingTree, cwdFiles
        );
        // 获取未暂存的修改
        ArrayList<String> unstaged = unstagedStatus(stagingArea, currBranchTrackingTree, cwdFiles);

        // 如果存在未追踪文件，则输出异常并退出
        if (!untracked.isEmpty() || !unstaged.isEmpty()) {
            Utils.exitWithError(
                "There is an untracked file in the way; delete it, or add and commit it first."
            );
        }

        // 重置工作目录文件到给定branch的状态
        resetCWDfiles(cwdFiles, branchTrackingTree);
        // 改写HEAD指向
        Utils.writeObject(HEAD, branchheadfile);
    }

    /** DESCRIPTION -- branch
     * Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch called “master”.
     */
    public static void branch(String branchName) {
        // 如果分支已存在，抛出异常并退出
        if (branchExists(branchName)) {
            Utils.exitWithError("A branch with that name already exists.");
        }

        // 读取当前头提交ID
        File currHeadfile = Utils.readObject(HEAD, File.class);
        String currHeadID = Utils.readContentsAsString(currHeadfile);
        // 新建提交文件
        File newBranchFile = Utils.join(HEADS_DIR, branchName);
        // 将当前头提交ID写入新建提交 --> 即将新建提交指向当前节点
        Utils.writeContents(newBranchFile, currHeadID);
    }

    private static void printStatus(String title, ArrayList<String> objects) {
        System.out.println(title);
        for (String object : objects) {
            System.out.println(object);
        }
        System.out.println();
    }

    private static void branchStatus(File currheadfile) {
        // 读取所有分支文件
        List<String> branchfilenames = Utils.plainFilenamesIn(HEADS_DIR);
        // 获取当前所处分支
        String currheadname = currheadfile.getName();
        System.out.println("=== Branches ===\n*"+currheadname);
        if (branchfilenames != null) {
            for (String branchfilename : branchfilenames) {
                if (branchfilename.equals(currheadname)) {
                    continue;
                }
                System.out.println(branchfilename);
            }
        }
        System.out.println();
    }

    private static void addStageStatus(Blobs StagingArea) {
        // 读取添加暂存区文件
        TreeMap<String, String> addStagingArea = StagingArea.getAddedFiles();
        ArrayList<String> addfilenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : addStagingArea.entrySet()) {
            addfilenames.add(entry.getKey());
        }
        printStatus("=== Staged Files ===", addfilenames);
    }

    private static void rmStageStatus(Blobs StagingArea) {
        // 读取删除暂存区文件
        TreeMap<String, String> rmStagingArea = StagingArea.getRemovedFiles();
        ArrayList<String> rmfilenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : rmStagingArea.entrySet()) {
            rmfilenames.add(entry.getKey());
        }
        printStatus("=== Removed Files ===", rmfilenames);
    }

    private static ArrayList<String> unstagedStatus(Blobs StagingArea, TreeMap<String, String> trackingFiles, List<String> cwdFiles) {
        // 记录文件状态：修改/删除未暂存
        ArrayList<String> unstaged = new ArrayList<>();

        // 遍历头提交文件
        for (Map.Entry<String, String> entry : trackingFiles.entrySet()) {
            // 如果文件存在于暂存区，跳过
            String[] findRes = StagingArea.find(entry.getKey());
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
                    unstaged.add(curr_filename + " (modified)");
                }
            }
            // 如果文件不存在
            else {
                // 则文件被删除且未加入暂存区
                unstaged.add(curr_filename + " (deleted)");
            }
        }

        return unstaged;
    }

    private static ArrayList<String> untrackedStatus(Blobs StagingArea, TreeMap<String, String> trackingFiles, List<String> cwdFiles) {
        // 存在于工作目录且不存在于追踪树的文件
        ArrayList<String> untracked = new ArrayList<>();
        for (String curr_filename : cwdFiles) {
            // 如果文件存在于暂存区，跳过
            String[] findRes = StagingArea.find(curr_filename);
            if (findRes[0] != null || findRes[1] != null) {
                continue;
            }
            String trackedSha1 = trackingFiles.get(curr_filename);
            // 新增文件，直接保存
            if (trackedSha1 == null) {
                untracked.add(curr_filename);
            }
        }
        return untracked;
    }

    /** DESCRIPTION -- status
     *  Displays what branches currently exist, and marks the current branch with a *.
     *  Also displays what files have been staged for addition or removal.
     *  An example of the exact format it should follow is as follows.
     *  === Branches ===
     *  === Staged Files ===
     *  === Removed Files ===
     *  === Modifications Not Staged For Commit ===
     *  === Untracked Files ===
     */
    public static void status() {
        // 当前头提交文件
        File currheadfile = Utils.readObject(HEAD, File.class);
        // 读取暂存区
        Blobs StagingArea = new Blobs(INDEX);
        // 读取头提交
        Commit head = readHeadCommit();
        // 读取头提交追踪文件
        TreeMap<String, String> trackingFiles = head.getTrackingTree().getAddedFiles();
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);

        // 输出分支状态
        branchStatus(currheadfile);
        // 输出add暂存区状态
        addStageStatus(StagingArea);
        // 输出remove暂存区状态
        rmStageStatus(StagingArea);
        // 输出未暂存的文件
        ArrayList<String> unstaged = unstagedStatus(StagingArea, trackingFiles, cwdFiles);
        printStatus("=== Modifications Not Staged For Commit ===", unstaged);
        // 输出未追踪的文件
        ArrayList<String> untracked = null;
        if (cwdFiles != null) {
            untracked = untrackedStatus(StagingArea,trackingFiles, cwdFiles);
        }
        printStatus("=== Untracked Files ===", untracked);
    }

    /** DESCRIPTION -- rm-branch
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     */
    public static void rmBranch(String branchName) {
        // 获取当前所处分支名
        String currBranchName = Utils.readObject(HEAD, File.class).getName();
        // 删除分支未当前所处分支，抛出异常退出
        if (currBranchName.equals(branchName)) {
            Utils.exitWithError("Cannot remove the current branch.");
        }
        // 要删除分支不存在，抛出异常退出
        if (!branchExists(branchName)) {
            Utils.exitWithError("A branch with that name does not exist.");
        }
        // 否则删除.gitlet/refs/heads目录下对应的分支文件
        File deleteBranchFile = Utils.join(HEADS_DIR, branchName);
        Utils.deleteFile(deleteBranchFile);
    }

    /** DESCRIPTION -- reset
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * See the intro for an example of what happens to the head pointer after using reset.
     * The [commit id] may be abbreviated as for checkout. The staging area is cleared.
     * The command is essentially checkout of an arbitrary commit that also changes the current branch head.
     * @param commitID
     */
    public static void reset(String commitID) {
        // 读取给定ID对应的提交
        Commit c = readCommitWithID(commitID);
        // 如果不存在这样的提交，抛出异常并退出
        if (c == null) {
            Utils.exitWithError("No commit with that id exists.");
        }

        // 读取给定提交追踪文件
        TreeMap<String, String> commitTrackingTree = c.getTrackingTree().getAddedFiles();
        // 读取头提交
        Commit currCommit = readHeadCommit();
        // 读取头提交追踪文件
        TreeMap<String, String> currCommitTrackingTree = currCommit.getTrackingTree().getAddedFiles();
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);
        // 读取暂存区
        Blobs stagingArea = new Blobs(INDEX);
        // 获取工作目录下未追踪文件
        ArrayList<String> untracked = untrackedStatus(stagingArea, currCommitTrackingTree, cwdFiles);
        // 如果存在未追踪文件，抛出异常并退出
        if (!untracked.isEmpty()) {
            Utils.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        // 重置工作目录文件到给定提交
        resetCWDfiles(cwdFiles, commitTrackingTree);

        // 如果分支不等，改写HEAD指向分支
        File newHead = Utils.join(HEADS_DIR, c.getBranch());
        Utils.writeObject(HEAD, newHead);
        // 改写头提交所处Commit ID
        Utils.writeContents(newHead, commitID);
    }

    /**
     * 获取给定分支提交的父提交（给定提交一定位于给定分支上）
     * @param c             给定提交
     * @param branchName    给定分支
     * @return              给定提交在给定分支上的父提交
     */
    private static Commit getParentCommit(Commit c, String branchName) {
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
     *
     * @param branchName
     * @return
     */
    private static Commit findSplitPoint(Commit currHead, Commit givenHead) {
        // 读取当前头提交到提交图中其他所有节点的距离
        TreeMap<String, Integer> currHeadDisTable = Commit.calDistance(currHead);
        // 读取给定头提交到提交图中其他所有节点的距离
        TreeMap<String, Integer> givenHeadDisTable = Commit.calDistance(givenHead);

        String nearestNodeID = null;
        int minDistance = 2*currHeadDisTable.size()+1;

        for(Map.Entry<String, Integer> e : currHeadDisTable.entrySet()) {
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
        return readCommitWithID(nearestNodeID);
    }


    /**
     * @param splitPointTracking 分割点所追踪的文件
     * @param branchTracking     分支头提交所追踪的文件
     * @return 返回文件比较结果：0-修改，1-删除，2-保持相同, 3-新增
     */
    private static ArrayList<TreeMap<String, String>> compareFiles(TreeMap<String, String> splitPointTracking, TreeMap<String, String> branchTracking) {
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
            }
            else {
                // 否则从提交树中删除该文件
                branchTracking.remove(entry.getKey());
                // 比较头提交和分割点中的文件内容，如果相同则未作改变
                if (findRes.equals(entry.getValue())) {
                    putIndex = 2;
                }
                // 否则文件被覆盖了
                else {
                    putIndex = 0;
                }
            }
            // 将文件状态写入对应的索引区
            compareRes.get(putIndex).put(entry.getKey(), findRes);
        }
        compareRes.set(3, branchTracking);
        return compareRes;
    }

    private static void rewriteFile(String filename, String content) {
        // 将新的内容重写进入文件
        File newFile = new File(filename);
        if (newFile.exists()) {
            Utils.restrictedDelete(newFile);
        }
        Utils.writeContents(newFile, content);
    }

    /** 根据给定的文件内容哈希值，如果文件存在则覆盖其内容，否则创建新文件写入。
     * @param filename
     * @param newSha1
     */
    private static void changeFileBaseOnNewSha1(String filename, String newSha1) {
        String newContent = readContentWithSha1(newSha1);
        // 将新的内容重写进入文件
        rewriteFile(filename, newContent);
    }


    private static String readContentWithSha1(String sha1) {
        if (sha1 == null) {
            return "";
        }
        File f = Utils.join(OBJECTS_DIR, sha1.substring(0,2), sha1.substring(2));
        String content = Utils.readContentsAsString(f);
        return content;
    }

    private static String conflictMessage(String currContent, String givenContent) {
        return "<<<<<<< HEAD\n" +
        currContent + "=======\n"
        + givenContent + ">>>>>>>\n";
    }

    private static void writeConflict(String filename, String currSha1, String givenSha1) {
        // 读取暂存区
        Blobs stagingArea = new Blobs(INDEX);
        String currContent = readContentWithSha1(currSha1);
        String givenContent = readContentWithSha1(givenSha1);
        String conflictMessage = conflictMessage(currContent, givenContent);
        // 重写冲突文件
        rewriteFile(filename, conflictMessage);
        // 将冲突文件加入add暂存区
        stagingArea.operateStagingArea(filename, "add");
        // 保存暂存区
        stagingArea.saveBlobs(INDEX);
    }

    /** 寻找当前分支和给定分支中相对于它们的公共祖先，修改冲突的部分：
     * 1. 在一个分支删除而另一个没删除且更改；
     * 2. 在两个分支都修改，但内容不一；
     * 3. 在两个分支都新增，但内容不一。
     * @param currBranchCmpRes      当前分支和分割点的比较结果：0-修改，1-删除，2-保持相同, 3-新增
     * @param branchCmpRes          当前分支和分割点的比较结果：0-修改，1-删除，2-保持相同, 3-新增
     * @return key:filename-value[file content in curr branch,file content in given branch]
     */
    private static boolean solveConflict(ArrayList<TreeMap<String, String>> currBranchCmpRes, ArrayList<TreeMap<String, String>> branchCmpRes) {
        boolean conflictExists = false;
        // 在当前分支删除，给定分支更改
        for (Map.Entry<String, String> e : currBranchCmpRes.get(1).entrySet()) {
            String findRes = branchCmpRes.get(0).get(e.getKey());
            if (findRes != null) {
                writeConflict(e.getKey(), e.getValue(), null);
                conflictExists = true;
            }
        }
        // 在给定分支删除，在当前分支修改
        for (Map.Entry<String, String> e : branchCmpRes.get(1).entrySet()) {
            String findRes = currBranchCmpRes.get(0).get(e.getKey());
            if (findRes != null) {
                writeConflict(e.getKey(), null, e.getValue());
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

    /** DESCRIPTION -- merge
     *
     * @param branchName
     */
    public static void merge(String branchName) {

        // 如果给定分支不存在，抛出异常并退出
        if (!branchExists(branchName)) {
            Utils.exitWithError("A branch with that name does not exist.");
        }
        // 读取暂存区
        Blobs stagingArea = new Blobs(INDEX);
        // 读取当前头提交ID
        String currHeadID = Utils.readContentsAsString(Utils.readObject(HEAD, File.class));
        // 读取当前头提交
        Commit currHead = readHeadCommit();
        // 读取当前头提交追踪树
        Blobs currTrackingTree = currHead.getTrackingTree();
        // 读取给定分支头提交ID
        String branchHeadID = Utils.readContentsAsString(Utils.join(HEADS_DIR, branchName));
        // 读取给定分支头提交
        Commit branchHead = readCommitWithID(branchHeadID);
        // 读取给定分支头提交追踪树
        Blobs givenTrackingTree = branchHead.getTrackingTree();

        // 寻找分割提交
        Commit splitPoint = findSplitPoint(currHead, branchHead);
        // 读取分割点提交追踪树
        Blobs splitPointTrackingTree = splitPoint.getTrackingTree();
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);
        // 检查工作目录未追踪文件
        ArrayList<String> untracked = untrackedStatus(stagingArea, currTrackingTree.getAddedFiles(), cwdFiles);

        // 如果给定分支等于当前所处分支，抛出异常并退出
        if (branchName.equals(Utils.readObject(HEAD, File.class).getName())) {
            Utils.exitWithError("Cannot merge a branch with itself.");
        }

        // 如果暂存区不为空，抛出异常并退出
        if (!stagingArea.isEmpty()) {
            Utils.exitWithError("You have uncommitted changes.");
        }

        // 如果工作目录存在未跟踪的变更，抛出异常并退出
        if (!untracked.isEmpty()) {
            Utils.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        // 如果分割点即给定分支头提交
        if (splitPoint.isSameID(branchHeadID)) {
            Utils.exitWithError("Given branch is an ancestor of the current branch.");
        }

        // 如果分割点即当前分支头提交，将当前分支头提交更新到给定分支头提交
        if (splitPoint.isSameID(currHeadID)) {
            // HEAD指向给定分支的头提交
            File currHeadCommitFile = Utils.readObject(HEAD, File.class);
            // 将给定分支头提交ID写入当前分支头文件
            Utils.writeContents(currHeadCommitFile, branchHeadID);
            // 更新文件
            resetCWDfiles(cwdFiles, givenTrackingTree.getAddedFiles());
            // 输出向前移动头提交的信息并退出
            Utils.exitWithError("Current branch fast-forwarded.");
        }


        // 当前分支头提交和分割点追踪文件的比较结果：0-修改，1-删除，2-保持相同, 3-新增
        ArrayList<TreeMap<String, String>> currBranchCmpRes = compareFiles(splitPointTrackingTree.getAddedFiles(), currTrackingTree.getAddedFiles());
        // 给定分支头提交和分割点追踪文件的比较结果：0-修改，1-删除，2-保持相同, 3-新增
        ArrayList<TreeMap<String, String>> branchCmpRes = compareFiles(splitPointTrackingTree.getAddedFiles(), givenTrackingTree.getAddedFiles());

        // 1. 自分割点以来：
        // 1.1 在给定分支中被修改 && 在当前分支中未被修改 => 将修改暂存
        for (Map.Entry<String, String> e : branchCmpRes.get(0).entrySet()) {
            String res = currBranchCmpRes.get(2).get(e.getKey());
            if (res != null) {
                // 用给定分支修改覆盖当前工作目录下文件
                changeFileBaseOnNewSha1(e.getKey(), e.getValue());
                // 将当前修改加入add暂存区
                stagingArea.operateStagingArea(e.getKey(),"add");
            }
        }
        // 1.2 在给定分支中被删除 && 在当前分支中未删除 => 将删除暂存并删除相应文件
        for (Map.Entry<String, String> e : branchCmpRes.get(1).entrySet()) {
            String res = currBranchCmpRes.get(2).get(e.getKey());
            if (res != null) {
                stagingArea.operateStagingArea(e.getKey(), "rm");
                Utils.deleteFile(e.getKey());
            }
        }
        // 1.3 不存在于分割点 && 仅存在于给定分支 => 将修改暂存
        for (Map.Entry<String, String> e : branchCmpRes.get(3).entrySet()) {
            changeFileBaseOnNewSha1(e.getKey(), e.getValue());
            stagingArea.operateStagingArea(e.getKey(), "add");
        }

        // 2. 自分割点以来：
        // 2.1 在给定分支中未修改 && 在当前分支中被修改 => 保持不动
        // 2.2 在给定分支中被修改 && 在当前分支中被修改 && 修改相同 => 保持不动
        // 2.3 不存在于分割点 && 仅存在于当前分支 => 保持不动

        // 2.3 在给定分支中被删除 && 在当前分支中被删除 && 工作目录中存在被删除文件 => 保持不动（即仍然不被合并提交所跟踪）
        // 2.4 存在于分割点 && 在给定分支中未修改 && 在当前分支不存在 => 保持不动（即仍然不被合并提交所跟踪）
        // => 保持不动（即仍然不被合并提交所跟踪）=> 保持不动（即仍然不被合并提交所跟踪）


        // 3. 自分割点以来：
        // 3.1 存在于分割点 && 在当前分支未修改 && 在给定分支已删除 => 删除且不在merge提交中跟踪
        for (Map.Entry<String, String> e : currBranchCmpRes.get(2).entrySet()) {
            String findRes = branchCmpRes.get(1).get(e.getKey());
            if (findRes != null) {
                Utils.restrictedDelete(e.getKey());
            }
        }
        // 3.2 存在于分割点 && 在给定分支中未修改 && 在当前分支不存在 => 删除且不在merge提交中跟踪

        // 保存暂存区
        stagingArea.saveBlobs(INDEX);

        // 存在冲突，抛出异常并退出
        if (solveConflict(currBranchCmpRes, branchCmpRes)) {
            Utils.exitWithError("Encountered a merge conflict.");
        }

        // 否则新建提交
        Commit c = new Commit(currHead, branchHead, branchName);
        c.saveCommit();
        // 清空暂存区
        clearStagingArea();
    }
}
