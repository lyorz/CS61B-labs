package gitlet;

import java.io.*;
import java.util.*;

public class Command {
    /** 辅助函数：查找给定名称的分支是否存在
     */
    private static boolean branchExists(String branchName) {
        // 读取所有branch
        List<String> branchNameList = Utils.plainFilenamesIn(Repository.HEADS_DIR);

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
        }
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
        // 获取头提交
        Commit head = myUtils.readHeadCommit();
        // 获取头提交追踪的Blobs对象
        Blobs headTrackingTree = head.getTrackingTree();
        // 读取暂存区
        Blobs stagingArea = myUtils.readStagingArea();
        // 文件对象
        File f = new File(addFile);

        if (!f.exists()) {
            Utils.exitWithError("File does not exist.");
        }

        // 查找add的文件是否在rm暂存区中
        String[] findRes = stagingArea.find(addFile);
        // 如果在rm暂存区
        if (findRes[1] != null) {
            // 则取消删除并退出
            stagingArea.remove(addFile);
            // 保存暂存区状态
            myUtils.saveStagingArea(stagingArea);
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
        stagingArea.operateStagingArea(addFile, "add");
        // 保存暂存区状态
        myUtils.saveStagingArea(stagingArea);
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
        Blobs b = myUtils.readStagingArea();
        // 暂存区没有追踪文件，报错并退出
        if (b.isEmpty()) {
            Utils.exitWithError("No changes added to the commit.");
        }
        // 新建提交
        Commit c = new Commit(message);
        // 保存提交
        c.saveCommit();
        myUtils.clearStagingArea();
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
        String branchName = myUtils.readCurrBranchName();
        // 读取头提交
        Commit head = myUtils.readHeadCommit();
        // 读取父提交
        Commit parent = myUtils.getParentCommit(head, branchName);

        // 打印头提交
        System.out.println(head.getLog());
        // 从头提交向前遍历
        while (parent != null) {
            System.out.println(parent.getLog());
            head = parent;
            parent = myUtils.getParentCommit(head, branchName);
        }

    }

    /** DESCRIPTION -- rm
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     */
    public static void rm(String rmfilename) {
        // 读取当前Blogs中暂存文件
        Blobs stagingArea = myUtils.readStagingArea();
        // 读取头提交追踪树
        Blobs headTrackingTree = myUtils.readHeadCommit().getTrackingTree();

        // 查找用户删除文件在暂存区
        String[] findRes = stagingArea.find(rmfilename);
        if (!(findRes[0] == null && findRes[1] == null)) {
            // 从暂存区中取消
            stagingArea.remove(rmfilename);
            // 重新保存Blobs对象
            myUtils.saveStagingArea(stagingArea);
        } else {
            // 否则查找文件是否被提交所跟踪
            String[] headTrackingRes = headTrackingTree.find(rmfilename);

            // 如果被当前提交跟踪
            if (headTrackingRes[0] != null) {
                // 将其加入删除暂存区
                stagingArea.operateStagingArea(rmfilename, "rm");
                myUtils.saveStagingArea(stagingArea);
                // 删除工作目录下的文件
                File rmFile = new File(rmfilename);
                if (rmFile.exists()) {
                    Utils.restrictedDelete(rmFile);
                }
            } else {
               Utils.exitWithError("No reason to remove the file.");
            }
        }

    }

    /** DESCRIPTION -- global-log
     *  Like log, except displays information about all commits ever made.
     *  The order of the commits does not matter.
     *  Hint:
     *  there is a useful method in gitlet.Utils that will help you iterate over files within a directory.
     * */
    public static void globalLog() {
        // 获取.gitlet/objects/下所有目录名
        String[] dir_list = Utils.DirnamesIn(Repository.OBJECTS_DIR);
        // 遍历目录列表
        for (String dir : dir_list) {
            // 读取.gitlet/objects/dir下所有文件
            List<String> filename_list = Utils.plainFilenamesIn(Utils.join(Repository.OBJECTS_DIR, dir));
            if (filename_list == null) { continue; }
            // 遍历文件
            for (String filename : filename_list) {
                File f = Utils.join(Repository.OBJECTS_DIR, dir, filename);
                // 使用异常捕获，当文件记录Commit对象时，打印log信息
                try {
                    Commit c = Utils.readObject(f, Commit.class);
                    System.out.println(c.getLog());
                } catch (IllegalArgumentException ignore) { }
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
        String[] dir_list = Utils.DirnamesIn(Repository.OBJECTS_DIR);
        // 标志是否存在message等于输入参数的提交
        boolean flag = false;
        // 遍历目录列表
        for (String dir : dir_list) {
            // 读取.gitlet/objects/dir下所有文件
            List<String> filename_list = Utils.plainFilenamesIn(Utils.join(Repository.OBJECTS_DIR, dir));
            if (filename_list == null) { continue; }
            // 遍历文件
            for (String filename : filename_list) {
                File f = Utils.join(Repository.OBJECTS_DIR, dir, filename);
                // 使用异常捕获，当文件记录Commit对象时，比较其message是否和输入参数相同
                try {
                    Commit c = Utils.readObject(f, Commit.class);
                    String id = c.sameMessage(message);
                    if (id != null) {
                        System.out.println(id);
                        flag = true;
                    }
                } catch (IllegalArgumentException ignored) { }
            }
        }
        // 如果flag仍为false，输入出错误消息
        if (!flag) {
            Utils.exitWithError("Found no commit with that message.");
        }
    }

    /** DESCRIPTION -- checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * */
    public static void checkoutResetfile(String filename) {
        // 获取头提交
        Commit head = myUtils.readHeadCommit();
        // 获取头提交追踪的Blobs对象
        Blobs b = head.getTrackingTree();
        String fileID = b.find(filename)[0];
        // 如果查找文件不为空
        if (fileID != null) {
            myUtils.overrideFile(fileID, filename);
        } else {
            // 抛出异常
            Utils.exitWithError("File does not exist in that commit.");
        }
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
        if (ID.length() < Utils.UID_LENGTH) {
            // 存储目录一定是.gitlet/objects/ID[:2]
            File commitDir = Utils.join(Repository.OBJECTS_DIR, ID.substring(0, 2));
            // 列举当前目录下所有文件
            List<String> filenames = Utils.plainFilenamesIn(commitDir);
            // 查找匹配文件
            for (String f : filenames) {
                if (myUtils.checkShortID(ID, f)) {
                    String totalID = ID.substring(0, 2) + f;
                    c = Commit.readCommitWithID(totalID);
                }
            }
        } else {
            c = Commit.readCommitWithID(ID);
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
            myUtils.overrideFile(fileID, filename);
        }
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
        // 读取头提交
        Commit currHead = myUtils.readHeadCommit();

        // 要切换的分支不存在，退出
        if (!branchExists(branchName)) {
            Utils.exitWithError("No such branch exists.");
        }
        // 如果头提交位于branchName分支，无需切换，退出
        if (branchName.equals(myUtils.readCurrBranchName())) {
            Utils.exitWithError("No need to checkout the current branch.");
        }

        // 读取branchName对应的头提交
        File branchheadfile = Utils.join(Repository.HEADS_DIR, branchName);
        String commitID = Utils.readContentsAsString(branchheadfile);
        Commit branchHead = Commit.readCommitWithID(commitID);
        // 读取切换的branch头提交对应追踪文件
        TreeMap<String, String> branchTrackingTree = null;
        if (branchHead != null) {
            branchTrackingTree = branchHead.getTrackingTree().getAddedFiles();
        }
        // 读取当前头提交追踪文件
        TreeMap<String, String> currBranchTrackingTree = currHead.getTrackingTree().getAddedFiles();

        // 读取暂存区
        Blobs stagingArea = myUtils.readStagingArea();
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);
        // 获取工作目录下未被当前提交追踪的文件
        ArrayList<String> untracked = myUtils.untrackedStatus();
        // 获取未暂存的修改
        ArrayList<String> notStaged = myUtils.notStagedStatus();

        // 如果存在未追踪文件，则输出异常并退出
        if (!untracked.isEmpty() || !notStaged.isEmpty()) {
            Utils.exitWithError(
                "There is an untracked file in the way; delete it, or add and commit it first."
            );
        }

        // 重置工作目录文件到给定branch的状态
        myUtils.resetCWDFiles(branchTrackingTree);
        // 改写HEAD指向
        Utils.writeObject(Repository.HEAD, branchheadfile);
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
        File currHeadfile = Utils.readObject(Repository.HEAD, File.class);
        String currHeadID = Utils.readContentsAsString(currHeadfile);
        // 新建提交文件
        File newBranchFile = Utils.join(Repository.HEADS_DIR, branchName);
        // 将当前头提交ID写入新建提交 --> 即将新建提交指向当前节点
        Utils.writeContents(newBranchFile, currHeadID);
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
        // 输出分支状态
        myUtils.branchStatus();
        // 输出add暂存区状态
        myUtils.addStageStatus();
        // 输出remove暂存区状态
        myUtils.rmStageStatus();
        // 输出未暂存的文件
        ArrayList<String> notStaged = myUtils.notStagedStatus();
        myUtils.printStatus("=== Modifications Not Staged For Commit ===", notStaged);
        // 输出未追踪的文件
        ArrayList<String> untracked = myUtils.untrackedStatus();
        myUtils.printStatus("=== Untracked Files ===", untracked);
    }

    /** DESCRIPTION -- rm-branch
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     */
    public static void rmBranch(String branchName) {
        // 获取当前所处分支名
        String currBranchName = Utils.readObject(Repository.HEAD, File.class).getName();
        // 删除分支未当前所处分支，抛出异常退出
        if (currBranchName.equals(branchName)) {
            Utils.exitWithError("Cannot remove the current branch.");
        }
        // 要删除分支不存在，抛出异常退出
        if (!branchExists(branchName)) {
            Utils.exitWithError("A branch with that name does not exist.");
        }
        // 否则删除.gitlet/refs/heads目录下对应的分支文件
        File deleteBranchFile = Utils.join(Repository.HEADS_DIR, branchName);
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
        Commit c = Commit.readCommitWithID(commitID);
        // 如果不存在这样的提交，抛出异常并退出
        if (c == null) {
            Utils.exitWithError("No commit with that id exists.");
        }

        // 读取给定提交追踪文件
        TreeMap<String, String> commitTrackingTree = c.getTrackingTree().getAddedFiles();
        // 读取头提交
        Commit currCommit = myUtils.readHeadCommit();
        // 读取头提交追踪文件
        TreeMap<String, String> currCommitTrackingTree = currCommit.getTrackingTree().getAddedFiles();
        // 读取工作目录文件
        List<String> cwdFiles = Utils.plainFilenamesIn(Repository.CWD);
        // 读取暂存区
        Blobs stagingArea = myUtils.readStagingArea();
        // 获取工作目录下未追踪文件
        ArrayList<String> untracked = myUtils.untrackedStatus();
        // 如果存在未追踪文件，抛出异常并退出
        if (!untracked.isEmpty()) {
            Utils.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        // 重置工作目录文件到给定提交
        myUtils.resetCWDFiles(commitTrackingTree);

        // 如果分支不等，改写HEAD指向分支
        File newHead = Utils.join(Repository.HEADS_DIR, c.getBranch());
        Utils.writeObject(Repository.HEAD, newHead);
        // 改写头提交所处Commit ID
        Utils.writeContents(newHead, commitID);
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
        Blobs stagingArea = myUtils.readStagingArea();
        // 读取当前头提交
        Commit currHead = myUtils.readHeadCommit();
        // 读取给定分支头提交ID
        String branchHeadID = Utils.readContentsAsString(Utils.join(Repository.HEADS_DIR, branchName));
        // 读取给定分支头提交
        Commit branchHead = Commit.readCommitWithID(branchHeadID);
        // 读取给定分支头提交追踪树
        Blobs givenTrackingTree = branchHead.getTrackingTree();

        // 寻找分割提交
        Commit splitPoint = myUtils.findSplitPoint(currHead, branchHead);
        // 检查工作目录未追踪文件
        ArrayList<String> untracked = myUtils.untrackedStatus();

        // 如果给定分支等于当前所处分支，抛出异常并退出
        if (branchName.equals(myUtils.readCurrBranchName())) {
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
        if (splitPoint.equals(branchHead)) {
            Utils.exitWithError("Given branch is an ancestor of the current branch.");
        }

        // 如果分割点即当前分支头提交，将当前分支头提交更新到给定分支头提交
        if (splitPoint.equals(currHead)) {
            // HEAD指向给定分支的头提交
            File currHeadCommitFile = Utils.readObject(Repository.HEAD, File.class);
            // 将给定分支头提交ID写入当前分支头文件
            Utils.writeContents(currHeadCommitFile, branchHeadID);
            // 更新文件
            myUtils.resetCWDFiles(givenTrackingTree.getAddedFiles());
            // 输出向前移动头提交的信息并退出
            Utils.exitWithError("Current branch fast-forwarded.");
        }


        // 当前分支头提交和分割点追踪文件的比较结果：0-修改，1-删除，2-保持相同, 3-新增
        ArrayList<TreeMap<String, String>> currBranchCmpRes = myUtils.compareFiles(splitPoint, currHead);
        // 给定分支头提交和分割点追踪文件的比较结果：0-修改，1-删除，2-保持相同, 3-新增
        ArrayList<TreeMap<String, String>> branchCmpRes = myUtils.compareFiles(splitPoint, branchHead);

        // 1. 自分割点以来：
        // 1.1 在给定分支中被修改 && 在当前分支中未被修改 => 将修改暂存
        for (Map.Entry<String, String> e : branchCmpRes.get(0).entrySet()) {
            String res = currBranchCmpRes.get(2).get(e.getKey());
            if (res != null) {
                // 用给定分支修改覆盖当前工作目录下文件
                myUtils.overrideFile(e.getValue(), e.getKey());
                // 将当前修改加入add暂存区
                stagingArea.operateStagingArea(e.getKey(), "add");
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
            myUtils.overrideFile(e.getValue(), e.getKey());
            stagingArea.operateStagingArea(e.getKey(), "add");
        }

        // 3. 自分割点以来：
        // 3.1 存在于分割点 && 在当前分支未修改 && 在给定分支已删除 => 删除且不在merge提交中跟踪
        for (Map.Entry<String, String> e : currBranchCmpRes.get(2).entrySet()) {
            String findRes = branchCmpRes.get(1).get(e.getKey());
            if (findRes != null) {
                Utils.restrictedDelete(e.getKey());
            }
        }

        // 保存暂存区
        myUtils.saveStagingArea(stagingArea);

        // 存在冲突，抛出异常
        if (myUtils.solveConflict(splitPoint, branchHead)) {
            System.out.println("Encountered a merge conflict.");
        }

        // 否则新建提交
        Commit c = new Commit(currHead, branchHead, branchName);
        c.saveCommit();
        // 清空暂存区
        myUtils.clearStagingArea();
    }
}
