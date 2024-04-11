package gitlet;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Blobs implements Serializable{
    /** 存储暂存区所有文件信息的文件路径 */
    private static final File  OBJECT = Utils.join(".gitlet", "objects");
    /** 添加文件暂存区 */
    private TreeMap<String, String> addStagingArea;
    /** 删除文件暂存区 */
    private TreeMap<String, String> rmStagingArea;

    /** 为什么创建了这样一个成员？
     * 是因为TreeMap似乎无法直接通过Utils.writeObject写入文件，所以考虑为其创建一个能够序列化的子类方便对象的读写。
     */
    private static class blob implements Serializable{
        /** 外部对该文件做出的操作：add/rm */
        String operation;
        /** 文件名 */
        String filename;
        /** 根据filename指向文件内容计算出的sha-1哈希值。 */
        String ID;
        /** blob对象构造函数 */
        public blob(String filename, String ID, String operation) {
            this.filename = filename;
            this.ID = ID;
            this.operation = operation;
        }
    }

    public Blobs() {
        addStagingArea = new TreeMap<>();
        rmStagingArea = new TreeMap<>();
    }


    public Blobs(File f) {
        addStagingArea = new TreeMap<>();
        rmStagingArea = new TreeMap<>();
        if (!f.exists() || f.length() == 0) {
            return;
        }
        fromfile(f);
    }

    /** 判断暂存区是否为空 */
    public boolean isEmpty() {
        return (addStagingArea.isEmpty() && rmStagingArea.isEmpty());
    }

    /** 将暂存区/提交追踪文件树内容写入文件f */
    public void saveBlobs(File f) {
        // 将add暂存区对象写入文件
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f))) {
            for (Map.Entry<String, String> entry : addStagingArea.entrySet()) {
                blob b = new blob(entry.getKey(), entry.getValue(), "add");
                outputStream.writeObject(b);
            }
            for (Map.Entry<String, String> entry : rmStagingArea.entrySet()) {
                blob b = new blob(entry.getKey(), entry.getValue(), "rm");
                outputStream.writeObject(b);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** 从文件f中读取暂存区/提交追踪文件树内容 */
    private void fromfile(File f) {
        // 从文件中逐个读取blob对象，并存入哈希表blobs
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
            while (true) {
                try {
                    blob b = (blob) inputStream.readObject();
                    if (b.operation.equals("add")) {
                        addStagingArea.put(b.filename, b.ID);
                    }
                    else {
                        rmStagingArea.put(b.filename, b.ID);
                    }
                } catch (EOFException e) {
                    break; // 读到文件末尾，退出循环
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }

    /** 根据输入operation将filename添加进入add/rm暂存区 */
    public void operateStagingArea(String filename, String operation) {
        if (operation.equals("add")) {
            put(filename, this.addStagingArea);
        }
        else {
            put(filename, this.rmStagingArea);
        }
    }

    private void put(String filename, TreeMap<String, String> StagingArea) {
        // 读取添加文件内容
        File addfile = new File(filename);
        String content = Utils.readContentsAsString(addfile);
        // 根据文件内容计算sha1哈希值
        String ID = Utils.sha1(content);


        /** 经过对git的验证发现无需在更新同一文件的add时删除原文件的备份内容。
         * String lastID = StagingArea.get(filename);
         * // lastID不为空 -> 暂存区原文件经过修改；否则暂存区中没有该文件
         *         if (lastID != null) {
         *             if (lastID.equals(ID)) {
         *                 return;
         *             }
         *             // 删除objects目录下的原文件备份
         *             File srcDir = Utils.join(OBJECT, lastID.substring(0,2));
         *             File srcCopy = Utils.join(srcDir, lastID.substring(2));
         *             srcCopy.delete();
         *
         *             if (srcDir.listFiles().length == 0) {
         *                 srcDir.delete();
         *             }
         *         }
         */

        StagingArea.put(filename, ID);

        // 写入新文件备份
        File dstDir = Utils.join(OBJECT, ID.substring(0,2));
        File dstCopy = Utils.join(dstDir, ID.substring(2));
        if (!dstDir.exists()) {
            dstDir.mkdir();
        }
        Utils.writeContents(dstCopy, content);
    }

    /** 查找提交/删除暂存区中是否存在以key为键的条目 */
    public String[] find(String key) {
        String[] res = new String[2];
        res[0] = addStagingArea.get(key);
        res[1] = rmStagingArea.get(key);
        return res;
    }

    /** 将键为key的条目从暂存区中移除 */
    public void remove(String key) {
        if (addStagingArea.get(key) != null) {
            addStagingArea.remove(key);
        }
        else {
            rmStagingArea.remove(key);
        }
    }

    /** 将键为keyname的条目指向备份内容从objects目录下删除
    public void removeCopy(String filename) {
        String rmfileID = addStagingArea.get(filename);

        File rmDir = Utils.join(OBJECT, rmfileID.substring(0,2));
        File rmfileCopy = Utils.join(rmDir, rmfileID.substring(2));

        rmfileCopy.delete();

        if (rmDir.listFiles().length == 0) {
            rmDir.delete();
        }
    }
    */

    /** 执行commit时检查新提交所追踪内容，输入TrackingTree为当前提交追踪文件 */
    public void checkforCommit(Blobs TrackingTree) {
        // 当rm暂存区不为空，且内容被当前提交所追踪时，需要从新提交中移除。
        for (Map.Entry<String, String> rmblob : this.rmStagingArea.entrySet()) {
            if (TrackingTree.addStagingArea.get(rmblob.getKey())!= null) {
                TrackingTree.addStagingArea.remove(rmblob.getKey());
            }
        }
        // 当add暂存区不为空，需要加入新提交
        for (Map.Entry<String, String> addblob : this.addStagingArea.entrySet()) {
            TrackingTree.operateStagingArea(addblob.getKey(), "add");
        }
        TrackingTree.rmStagingArea.clear();
    }

    /** 返回Blobs对象成员的字符串形式 */
    public String toString() {
        String returnItem = "";
        for (Map.Entry<String, String> rmblob : this.rmStagingArea.entrySet()) {
            returnItem = returnItem + rmblob.getKey() + rmblob.getValue();
        }
        // 当add暂存区不为空，需要加入新提交
        for (Map.Entry<String, String> addblob : this.addStagingArea.entrySet()) {
            returnItem = returnItem + addblob.getKey() + addblob.getValue();
        }
        return returnItem;
    }
}
