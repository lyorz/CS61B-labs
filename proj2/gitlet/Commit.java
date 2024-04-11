package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author lyorz
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    static final File OBJECTS_DIR = Utils.join(".gitlet", "objects");
    static final File HEAD = Utils.join(".gitlet", "HEAD");
    private static final File INDEX = Utils.join(".gitlet", "index");
    /** The message of this Commit. */
    private String message;

    /** commit创建时的时间戳：从1970-01-01 00:00:00 至当前时间的毫秒数*/
    private String timestamp;

    /** 当前commit的父节点sha-1哈希值 */
    private String parent;

    /** 当前Commit对象的sha-1哈希值 */
    private String ID;

    /** 当前Commit下，记录所有文件快照的sha-1哈希值*/
    private String tree;
    /** 构造函数，接收message，实例化Commit对象
     * （由于默认初始提交message为“init commit”所以可以区分初始提交和其他提交）
     */
    public Commit(String message) {
        this.message = message;
        // 初始提交
        if (message.equals("initial commit")) {
            this.timestamp = Utils.getTimeString(0);
            this.parent = "";
            this.tree =  "";
        }
        // 非初始提交
        else {
            // 读取父提交
            String parentCommitID = Utils.readContentsAsString(HEAD);
            Commit parentCommit = Commit.fromfile(Utils.join(OBJECTS_DIR, parentCommitID.substring(0,2), parentCommitID.substring(2)));
            Blobs newTree = parentCommit.getBlobsofTree();

            // 根据父提交和newBlob（暂存区及父提交内容）创建当前commit
            this.timestamp = Utils.getTimeString(System.currentTimeMillis());
            this.parent = parentCommitID;
            this.tree = Utils.sha1(newTree.toString());

            saveTree(newTree);
        }
        this.ID = Utils.sha1(this.message, this.timestamp, this.parent, this.tree);
        // 改写HEAD
        Utils.writeContents(HEAD, this.ID);
    }

    /** 从输入文件f中读取Commit对象 */
    public static Commit fromfile(File f) {
        return Utils.readObject(f, Commit.class);
    }

    /** 将Commit对象保存在文件中
     * 路径为：.gitlet/objects/ID[:2]/ID[2:]
     */
    public void saveCommit() {
        File outDir = Utils.join(OBJECTS_DIR, this.ID.substring(0,2));
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        File outFile = Utils.join(outDir, this.ID.substring(2));
        Utils.writeObject(outFile, this);
    }

    /** 返回当前提交的父提交节点
     * （如果本身是初始提交则返回null）
     */
    public Commit getParent() {
        // 如果已经是头节点，返回null
        if (this.parent.equals("")) {
            return null;
        }
        // 否则读取父commit对象
        File parentCommitFile = Utils.join(OBJECTS_DIR, this.parent.substring(0,2), this.parent.substring(2));
        Commit parentCommit = fromfile(parentCommitFile);
        return parentCommit;
    }

    /** 返回当前提交的Log信息，输出统一格式的字符串。*/
    public String getLog() {
        String returnStr = "===\n";
        // 添加commit对应sha-1哈希值
        returnStr = returnStr + "commit " + this.ID + "\n";
        // 添加提交日期
        returnStr = returnStr + "Date: " + this.timestamp + "\n";
        // 添加提交信息
        returnStr = returnStr + this.message + "\n";

        return returnStr;
    }

    /** 返回头提交所追踪的文件树构成的Blobs对象 */
    public Blobs getTrackingTree() {
        Blobs TrackingTree = new Blobs();
        // 若当前提交存在跟踪文件
        if (!this.tree.equals("")) {
            File trackingFile = Utils.join(OBJECTS_DIR, this.tree.substring(0,2),this.tree.substring(2));
            TrackingTree = new Blobs(trackingFile);
        }
        return TrackingTree;
    }

    /** 返回新提交追踪文件列表的Blobs对象。
     * （同时考虑当前提交所追踪的文件，并根据暂存区进行增减。）
     */
    public Blobs getBlobsofTree() {
        Blobs StagingTree = new Blobs(INDEX);
        Blobs TrackingTree = getTrackingTree();
        StagingTree.checkforCommit(TrackingTree);

        return TrackingTree;
    }

    /** 保存当前提交追踪文件
     * （保存路径为：.gitlet/objects/tree[:2]/tree[2:]）
     */
    public void saveTree(Blobs tree) {
        File saveDir = Utils.join(OBJECTS_DIR, this.tree.substring(0,2));
        File saveFile = Utils.join(saveDir, this.tree.substring(2));

        if (!saveDir.exists()) {
            saveDir.mkdir();
        }

        tree.saveBlobs(saveFile);
    }

    /** 若输入message和当前提交相同，则打印出当前提交ID，并返回true */
    public String sameMessage(String message) {
        if (this.message.equals(message)) {
            return this.ID;
        }
        return null;
    }

    /** 判断输入ID是否等于当前提交 */
    public boolean isSameID(String ID) {
        return this.ID.equals(ID);
    }
}
