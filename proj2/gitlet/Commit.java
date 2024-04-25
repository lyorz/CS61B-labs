package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;


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

    private static final File HEADS_DIR = Utils.join(".gitlet", "refs", "heads");
    /** The message of this Commit. */
    private String message;

    /** commit所处分支 */
    private String branch;

    /** commit创建时的时间戳：从1970-01-01 00:00:00 至当前时间的毫秒数*/
    private String timestamp;

    /** 当前commit的父节点sha-1哈希值 */
    private String parent;

    /** 当前Commit的父节点sha-1哈希值（专用于merge提交） */
    private String sencondParent;

    /** 当前Commit对象的sha-1哈希值 */
    private String ID;

    /** 当前Commit下，记录所有文件快照的sha-1哈希值*/
    private String tree;
    /** 构造函数，接收message，实例化Commit对象
     * （由于默认初始提交message为“init commit”所以可以区分初始提交和其他提交）
     */
    public Commit(String message) {
        this.message = message;
        this.sencondParent = null;
        File commitfile = new File(HEADS_DIR, "master");
        // 初始提交
        if (message.equals("initial commit")) {
            this.timestamp = Utils.getTimeString(0);
            this.parent = "";
            this.tree =  "";
            this.branch = "master";
        }
        // 非初始提交
        else {
            // 读取父提交
            commitfile = Utils.readObject(HEAD, File.class);
            String parentCommitID = Utils.readContentsAsString(commitfile);
            Commit parentCommit = Commit.fromfile(Utils.join(OBJECTS_DIR, parentCommitID.substring(0,2), parentCommitID.substring(2)));
            Blobs newTree = parentCommit.getBlobsofTree();

            // 根据父提交和newBlob（暂存区及父提交内容）创建当前commit
            this.timestamp = Utils.getTimeString(System.currentTimeMillis());
            this.parent = parentCommitID;
            this.tree = Utils.sha1(newTree.toString());
            this.branch = Utils.readObject(HEAD, File.class).getName();

            saveTree(newTree);
        }
        this.ID = Utils.sha1(this.message, this.timestamp, this.parent, this.tree);
        // 改写HEAD
        Utils.writeObject(HEAD, commitfile);
        Utils.writeContents(commitfile, this.ID);
    }

    /**
     * 构造函数，接收当前分支的头提交、给定分支的头提交，本函数特定为merge行为实例化Commit对象。
     * @param currHead 当前分支头提交
     * @param givenHead 给定分支头提交
     */
    public Commit(Commit currHead, Commit givenHead, String branchName) {
        // 读取头提交文件对象
        File commitfile = Utils.readObject(HEAD, File.class);
        Blobs newTree = currHead.getBlobsofTree();

        this.parent = currHead.ID;
        this.sencondParent = givenHead.ID;
        this.message = "Merged " + branchName + " into " + commitfile.getName() +".";
        this.timestamp = Utils.getTimeString(System.currentTimeMillis());
        this.tree = Utils.sha1(newTree.toString());
        this.branch = currHead.branch;
        this.ID = Utils.sha1(this.message, this.timestamp, this.parent, this.sencondParent, this.tree);
        saveTree(newTree);
        // 改写HEAD
        Utils.writeObject(HEAD, commitfile);
        Utils.writeContents(commitfile, this.ID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Commit) {
            Commit c = (Commit) obj;
            return ID.equals(c.ID);
        }
        return false;
    }

    /**
     * 判断当前提交是否为merge的结果。
     * @return 如果当前提交是merge的结果，返回true
     */
    public boolean isMergedCommit() {
        return this.sencondParent != null;
    }

    /** 获取当前commit所处分支 */
    public String getBranch() {
        return this.branch;
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
        if (this.parent.isEmpty()) {
            return null;
        }
        // 否则读取父commit对象
        File parentCommitFile = Utils.join(OBJECTS_DIR, this.parent.substring(0,2), this.parent.substring(2));
        return fromfile(parentCommitFile);
    }

    public Commit getSecondParent() {
        // 如果已经是头节点，返回null
        if (this.parent.isEmpty()) {
            return null;
        }
        // 否则读取第二父提交对象
        // 否则读取父commit对象
        File parentCommitFile = Utils.join(OBJECTS_DIR, this.sencondParent.substring(0,2), this.sencondParent.substring(2));
        return fromfile(parentCommitFile);
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

    /**
     * 将全局所有提交描述为一张图，以邻接表结构表示。
     * @return 返回所有提交构成的图
     */
    public static TreeMap<String, ArrayList<String>> getCommitGraph() {
        TreeMap<String, ArrayList<String>> graph = new TreeMap<>();
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
                // 使用异常捕获，当文件记录Commit对象时，存入图
                try {
                    Commit c = Utils.readObject(f, Commit.class);
                    // 如果图中还没有该节点，创建。
                    graph.computeIfAbsent(c.ID, k -> new ArrayList<>());
                    // 记录该提交节点的父节点:
                    ArrayList<String> node_parents = graph.get(c.ID);
                    // 如果是merge提交的节点，将第二父节点记录进入表
                    if (c.isMergedCommit()) {
                        node_parents.add(c.getSecondParent().ID);
                    }
                    // 如果存在父节点，将父节点记录进入表
                    if (c.getParent() != null) {
                        node_parents.add(c.getParent().ID);
                    }
                }
                // 否则不做处理
                catch (IllegalArgumentException ignore) { }
            }
        }
        return graph;
    }

    /**
     * 计算节点src到图中各节点的最短距离
     * @param src   提交ID
     * @return      记录节点src到达图中各节点的距离（不可达标记为-1，到自身距离为0）
     */
    public static TreeMap<String, Integer> calDistance(Commit src) {
        TreeMap<String, ArrayList<String>> graph = getCommitGraph();
        TreeMap<String, Integer> path_length = new TreeMap<>();
        TreeMap<String, Boolean> flags = new TreeMap<>();
        Queue<String> que = new LinkedList<>();
        que.add(src.ID);

        // 初始化距离表和标记数组
        for (Map.Entry<String, ArrayList<String>> node : graph.entrySet()) {
            if (src.ID.equals(node.getKey())) {
                path_length.put(node.getKey(), 0);
                flags.put(node.getKey(), true);
                continue;
            }
            path_length.put(node.getKey(), -1);
            flags.put(node.getKey(), false);
        }

        // 当队列不为空
        while (!que.isEmpty()) {
            // 弹出队列首端元素
            String nodeID = que.remove();
            // 获取当前src到该节点的距离
            int curr_length = path_length.get(nodeID);
            // 获取当前节点的邻居节点
            List<String> adj = graph.get(nodeID);
            for (String neighborNode : adj) {
                if (!flags.get(neighborNode)) {
                    // 更新src到邻居节点的距离
                    path_length.put(neighborNode, curr_length+1);
                    // 更新邻居节点访问标记
                    flags.put(neighborNode, true);
                    // 邻居节点入队
                    que.add(neighborNode);
                }
            }
        }
        return path_length;
    }

}
