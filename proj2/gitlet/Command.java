package gitlet;

import java.io.*;
import java.sql.Blob;
import java.util.List;

public class Command {
    private static final File INDEX = Utils.join(".gitlet", "index");
    static final File HEAD = Utils.join(".gitlet", "HEAD");
    static final File OBJECTS_DIR = Utils.join(".gitlet", "objects");

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
        // 初始化.gitlet/
        if (Repository.setupPersistence()) {
            // 创建初始提交
            Commit c = new Commit("Init commit.");
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
    public static void add(String addfile) {
        File f = new File(addfile);
        if (!f.exists()) {
            Utils.exitWithError("File does not exist.");
        }
        Blobs b = new Blobs(INDEX);
        b.operateStagingArea(addfile, "add");
        b.saveBlobs(INDEX);
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
        // 暂存区没有追踪文件
        Blobs b = new Blobs(INDEX);
        if (b.isEmpty()) {
            Utils.exitWithError("No changes added to the commit.");
        }
        // 读取头提交
        Commit c = new Commit(message);
        c.saveCommit();
        // 清空暂存区
        try{
            FileWriter writer = new FileWriter(INDEX);
            writer.write("");
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** DESCRIPTION -- log
     * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents found in merge commits.
     * (In regular Git, this is what you get with git log --first-parent).
     * This set of commit nodes is called the commit’s history.
     * For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.
     * */
    public static void log() {
        // 尚未解决：合并提交？

        // 读取当前提交和父提交
        String commitID = Utils.readContentsAsString(HEAD);
        Commit commit = Commit.fromfile(Utils.join(OBJECTS_DIR, commitID.substring(0,2), commitID.substring(2)));
        Commit parent = commit.getParent();

        // 输出log信息
        while (parent != null) {
            System.out.println(commit.getLog());
            commit = parent;
            parent = commit.getParent();
        }
        System.out.println(commit.getLog());
    }

    /** DESCRIPTION -- rm
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * @param rmfilename: 待删除文件名
     */
    public static void rm(String rmfilename) {
        // 读取当前Blogs中暂存文件
        Blobs b = new Blobs(INDEX);
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
            File rmfile = new File(rmfilename);
            String rmfileID = Utils.sha1(Utils.readContentsAsString(rmfile));
            File rmfileCopy = Utils.join(OBJECTS_DIR, rmfileID.substring(0,2) , rmfileID.substring(2));

            // 如果被当前提交跟踪
            if (rmfileCopy.exists()) {
                // 将其加入删除暂存区
                b.operateStagingArea(rmfilename, "rm");
                b.saveBlobs(INDEX);
                // 删除工作目录下的文件
                rmfile.delete();
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
                catch (IllegalArgumentException e) { }
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
        if (flag == false) {
            Utils.exitWithError("Found no commit with that message.");
        }
    }

    /** checkout辅助函数，文件重写 */
    private static void overrideFile(String fileID, String filename) {
        // 读取文件内容
        File filecontent = Utils.join(OBJECTS_DIR, fileID.substring(0, 2), fileID.substring(2));
        String strcontent = Utils.readContentsAsString(filecontent);
        System.out.println(strcontent);
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
        // 获取头提交ID
        String ID = Utils.readContentsAsString(HEAD);
        // 读取头提交的Commit对象
        File commitfile = Utils.join(OBJECTS_DIR, ID.substring(0,2), ID.substring(2));
        Commit head = Commit.fromfile(commitfile);
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

    /** DESCRIPTION -- checkout [commit id] -- [file name]
     *  Takes the version of the file as it exists in the commit with the given id,
     *  and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one.
     *  The new version of the file is not staged.
     */
    public static void checkoutLastfile(String ID, String filename) {
        // 获取头提交ID
        String headID = Utils.readContentsAsString(HEAD);
        // 读取头提交的Commit对象
        File commitfile = Utils.join(OBJECTS_DIR, headID.substring(0,2), headID.substring(2));
        Commit c= Commit.fromfile(commitfile);

        while (!c.isSameID(ID) && (c.getParent() != null)) {
            c = c.getParent();
        }

        // 如果找到了对应提交
        if (c.isSameID(ID)) {

            // 获取提交追踪文件树
            Blobs b = c.getTrackingTree();
            String fileID = b.find(filename)[0];
            if (fileID != null) {
                overrideFile(fileID, filename);
            }
            else {
                Utils.exitWithError("File does not exist in that commit.");
            }
        }
        else {
            Utils.exitWithError("No commit with that id exists.");
        }
    }

}
