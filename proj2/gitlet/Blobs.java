package gitlet;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;



public class Blobs implements Serializable {
    /** 添加文件暂存区 */
    private final TreeMap<String, String> addStagingArea;
    /** 删除文件暂存区 */
    private final TreeMap<String, String> rmStagingArea;

    /** 为什么创建了这样一个成员？
     * 是因为TreeMap似乎无法直接通过Utils.writeObject写入文件，所以考虑为其创建一个能够序列化的子类方便对象的读写。
     */
    private static class Blob implements Serializable {
        /** 外部对该文件做出的操作：add/rm */
        String operation;
        /** 文件名 */
        String filename;
        /** 根据filename指向文件内容计算出的sha-1哈希值。 */
        String ID;
        /** blob对象构造函数 */
        Blob(String filename, String ID, String operation) {
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
        fromFile(f);
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
                Blob b = new Blob(entry.getKey(), entry.getValue(), "add");
                outputStream.writeObject(b);
            }
            for (Map.Entry<String, String> entry : rmStagingArea.entrySet()) {
                Blob b = new Blob(entry.getKey(), entry.getValue(), "rm");
                outputStream.writeObject(b);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** 从文件f中读取暂存区/提交追踪文件树内容 */
    private void fromFile(File f) {
        // 从文件中逐个读取blob对象，并存入哈希表blobs
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
            while (true) {
                try {
                    Blob b = (Blob) inputStream.readObject();
                    if (b.operation.equals("add")) {
                        addStagingArea.put(b.filename, b.ID);
                    } else {
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
        } else {
            // 如果文件已删除
            File f = new File(filename);
            if (!f.exists()) {
                rmStagingArea.put(filename, "");
            } else {
                put(filename, this.rmStagingArea);
            }
        }
    }

    private void put(String filename, TreeMap<String, String> StagingArea) {
        // 读取添加文件内容
        File addfile = new File(filename);
        String content = Utils.readContentsAsString(addfile);
        // 根据文件内容计算sha1哈希值
        String ID = Utils.sha1(content);

        StagingArea.put(filename, ID);

        // 写入新文件备份
        File dstDir = Utils.join(Repository.OBJECTS_DIR, ID.substring(0, 2));
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
        if (rmStagingArea.get(key) != null) {
            rmStagingArea.remove(key);
        }
    }

    /** 执行commit时检查新提交所追踪内容，输入TrackingTree为当前提交追踪文件 */
    public void checkForCommit(Blobs TrackingTree) {
        // 当rm暂存区不为空，且内容被当前提交所追踪时，需要从新提交中移除。
        for (Map.Entry<String, String> rmBlob : this.rmStagingArea.entrySet()) {
            if (TrackingTree.addStagingArea.get(rmBlob.getKey()) != null) {
                TrackingTree.addStagingArea.remove(rmBlob.getKey());
            }
        }
        // 当add暂存区不为空，需要加入新提交
        for (Map.Entry<String, String> addBlob : this.addStagingArea.entrySet()) {
            TrackingTree.operateStagingArea(addBlob.getKey(), "add");
        }
        TrackingTree.rmStagingArea.clear();
    }

    /** 返回Blobs对象成员的字符串形式 */
    public String toString() {
        StringBuilder returnItem = new StringBuilder();
        for (Map.Entry<String, String> rmBlob : this.rmStagingArea.entrySet()) {
            returnItem.append(rmBlob.getKey()).append(rmBlob.getValue());
        }
        // 当add暂存区不为空，需要加入新提交
        for (Map.Entry<String, String> addBlob : this.addStagingArea.entrySet()) {
            returnItem.append(addBlob.getKey()).append(addBlob.getValue());
        }
        return returnItem.toString();
    }

    /** 获取add暂存区文件列表（对于追踪树来说意味着追踪文件列表） */
    public TreeMap<String, String> getAddedFiles() {
        return addStagingArea;
    }

    /** 获取rm暂存区文件列表*/
    public TreeMap<String, String> getRemovedFiles() {
        return rmStagingArea;
    }
}
