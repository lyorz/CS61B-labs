package gitlet;

import java.io.File;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  文件目录结构：
 *  .gitlet/ -- top level folder
 *  @author lyorz
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The .gitlet/objects directory. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The .gitlet/refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The .gitlet/refs/heads directory. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    public static final File INDEX = join(GITLET_DIR, "index");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static boolean setupPersistence() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            OBJECTS_DIR.mkdir();
            REFS_DIR.mkdir();
            HEADS_DIR.mkdir();
            return true;
        }
        return false;
    }
}
