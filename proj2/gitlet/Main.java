package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author lyorz
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // 异常命令处理
        // case1: 未输入参数
        if (args.length == 0) {
            Utils.exitWithError("Please enter a command.");
        }


        String firstArg = args[0];
        // case2: 工作目录下未初始化.gitlet
        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            Utils.exitWithError("Not in an initialized Gitlet directory.");
        }
        switch(firstArg) {
            case "init":
                Command.init();
                break;
            case "add":
                Command.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2, "Please enter a commit message.");
                Command.commit(args[1]);
                break;
            case "log":
                Command.log();
                break;
            case "rm":
                validateNumArgs(args, 2, "No reason to remove the file.");
                Command.rm(args[1]);
                break;
            case "global-log":
                Command.globalLog();
                break;
            case "find":
                Command.find(args[1]);
                break;
            case "checkout":
                if (args.length == 3) {
                    Command.checkoutResetfile(args[2]);
                }
                else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        Utils.exitWithError("Incorrect operands.");
                    }
                    Command.checkoutLastfile(args[1], args[3]);
                }
                else {
                    Command.checkout(args[1]);
                }
                break;
            case "branch":
                Command.branch(args[1]);
                break;
            case "status":
                Command.status();
                break;
            case "rm-branch":
                Command.rmBranch(args[1]);
                break;
            case "reset":
                Command.reset(args[1]);
                break;
            case "merge":
                Command.merge(args[1]);
                break;
            default:
                Utils.exitWithError("No command with that name exists.");
        }
    }
    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n, String message) {
        if (args.length != n) {
            throw new RuntimeException(message);
        }
    }
}
