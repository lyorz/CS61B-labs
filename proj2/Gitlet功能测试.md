# Gitlet功能测试

> @lyorz
>
> 项目说明：https://sp21.datastructur.es/materials/proj/proj2/proj2

命令进度：

- [x] init
- [x] add
- [x] commit
- [x] rm
- [x] log
- [x] global-log
- [x] find
- [x] status
- [x] checkout
- [x] branch
- [x] rm-branch
- [x] reset
- [x] merge

## 0406-0408：设计目录结构&完成init和add

大概是从4.6开始慢慢上手写的，设计文件目录结构实现init和add花了三天。

（实际上这个目录结构为了适应后来的功能开发还是在不断地变化。）

## 0409：完成Commit

> 2024/04/09

下面对commit进行功能测试。

### (1) 基本功能测试

**首先初始化gitlet**，init之后的目录结构：

```
│  HEAD
│
├─objects
│  └─18
│          a5bd36d2c406642225e9407d62ae6e20cf0bcf
│
└─refs
    └─heads
```

此时HEAD指向18a5bd36d2c406642225e9407d62ae6e20cf0bcf



然后我们**新建一个hello.txt并add**：

```
│  HEAD
│  index
│
├─objects
│  ├─18
│  │      a5bd36d2c406642225e9407d62ae6e20cf0bcf
│  │
│  └─da
│          39a3ee5e6b4b0d3255bfef95601890afd80709
│
└─refs
    └─heads
```

可以看到.gitlet目录下多出了index文件，我们用它来维护暂存区文件状态，并且add之后也多出了一个文件da39a3ee。

此时HEAD仍然指向：18a5bd36d2c406642225e9407d62ae6e20cf0bcf



**现在执行提交**：

```
│  HEAD
│  index
│
├─objects
│  ├─18
│  │      a5bd36d2c406642225e9407d62ae6e20cf0bcf
│  │
│  ├─2d
│  │      a338f30db190e075f79611d73d0d8eb0a74dba
│  │
│  ├─da
│  │      39a3ee5e6b4b0d3255bfef95601890afd80709
│  │
│  └─f4
│          5454bd4cca7b893bf387edf55b9025d5251d7d
│
└─refs
    └─heads
```

此时HEAD指向变为：2da338f30db190e075f79611d73d0d8eb0a74dba

多出的f45454bd4cca7b893bf387edf55b9025d5251d7d记录着我们创建的第一个提交所维护的文件快照。



### (2) 异常检测测试

- [x] 暂存区没有追踪文件，打印消息“No changes added to the commit.”并退出；
- [x] 提交命令必须给出message，否则打印消息“Please enter a commit message.”



## 0410: 完成log和rm

### log

当前分支提交信息一览。

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. `java gitlet.Main add hello.txt`
3. `java gitlet.Main commit "create hello.txt"`
4. `java gitlet.Main add hello.txt`
5. `java gitlet.Main commit "modify hello.txt"`



测试log命令：

```
===
commit 27d5f5e10d08741044b15d49021530dec691dbac
Date: 08:48:09 UTC, Wednesday, 10 April 2024
modify hello.txt

===
commit 9573e1929632be5dbf3643048d670903d06c5a51
Date: 08:42:03 UTC, Wednesday, 10 April 2024
create hello.txt

===
commit 18a5bd36d2c406642225e9407d62ae6e20cf0bcf
Date: 00:00:00 UTC, Thursday, 1 January 1970
Init commit.

```



#### (2) 异常检测测试

无异常检测。



### rm

删除文件/撤销暂存

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. `java gitlet.Main add hello.txt`
3. `java gitlet.Main commit "create hello.txt"`



**case1：修改hello.txt并add到暂存区，然后将其删除**

1. java gitlet.Main add hello.txt
2. java gitlet.Main rm hello.txt
3. java gitlet.Main commit "modify hello.txt"

```
预期输出：No changes added to the commit.
实际输出：No changes added to the commit.
```



**case2：在case1的基础上继续删除**

```
预期输出：No reason to remove the file.
实际输出：No reason to remove the file.
```



**~~case3：在case2的基础上add并提交之后，执行删除，并再次提交，可以观察到新提交用于追踪的文件中应当不包含hello.txt~~**

```
预期tree：<空>
实际tree：<空>
```

> 一开始在想一个问题：==如果存在两个文件名称不同但内容相同的文件a和b，当执行rm a时，其内容备份也将从objects中删除，会影响b的存储？==
>
> 后来发现这个地方是我理解错了，描述中并没有说要删除已经备份的内容。

#### (2) 异常检测测试

- [x] 当文件既没有被暂存也没有被头提交跟踪时，抛出错误“No reason to remove the file.”



## 0411：完成global-log、两个基本checkout、find

### global-log

全局提交信息一览（所有分支）。

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. `java gitlet.Main add hello.txt`
3. `java gitlet.Main commit "create hello.txt"`
4. `java gitlet.Main add hello.txt`
5. `java gitlet.Main commit "modify hello.txt"`
6. `java gitlet.Main add world.txt`
7. `java gitlet.Main commit "create world.txt"`



执行log命令输出：

```
===
commit c8c45cb3541de0ef08ddfb5e255ca43d9b7b8ed3
Date: 08:37:58 UTC, Thursday, 11 April 2024
create world.txt

===
commit 4807490cea278af2c57383111a7de8bb1621cf06
Date: 08:37:18 UTC, Thursday, 11 April 2024
modify hello.txt

===
commit d8849b488cfa9e2cd82a90b7ac94e5d39f11ba23
Date: 08:36:43 UTC, Thursday, 11 April 2024
create hello.txt

===
commit 18a5bd36d2c406642225e9407d62ae6e20cf0bcf
Date: 00:00:00 UTC, Thursday, 1 January 1970
Init commit.
```

执行global-log输出：

```
===
commit 18a5bd36d2c406642225e9407d62ae6e20cf0bcf
Date: 00:00:00 UTC, Thursday, 1 January 1970
Init commit.

===
commit 4807490cea278af2c57383111a7de8bb1621cf06
Date: 08:37:18 UTC, Thursday, 11 April 2024
modify hello.txt

===
commit c8c45cb3541de0ef08ddfb5e255ca43d9b7b8ed3
Date: 08:37:58 UTC, Thursday, 11 April 2024
create world.txt

===
commit d8849b488cfa9e2cd82a90b7ac94e5d39f11ba23
Date: 08:36:43 UTC, Thursday, 11 April 2024
create hello.txt
```

总共四条提交记录，但顺序不同于log命令的输出，测试通过。



#### (2) 异常检测测试

无异常



### find

和global-log差不多，就不写测试记录了。



### checkout -- [filename]

撤销文件修改。

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. `java gitlet.Main add hello.txt`
3. `java gitlet.Main commit "create hello.txt"`

上述提交中我们hello.txt的内容为“hello”。

**case1：修改文件但未提交至暂存区**

现在将内容修改为“test”并保存退出，不执行add直接执行`gitlet.Main checkout -- hello.txt`

```
预期hello.txt文件内容：hello
实际hello.txt文件内容：hello
```

**case2：文件删除但未提交至暂存区**

执行：

+ gitlet.Main rm hello.txt
+ gitlet.Main checkout -- hello.txt

```
预期：工作目录下重新出现hello.txt并且内容为hello
实际：符合预期
```



#### (2) 异常检测测试

- [x] filename不存在时打印错误消息。



### checkout [commit id] -- [filename]

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. `java gitlet.Main add hello.txt`
3. `java gitlet.Main commit "create hello.txt"`
4. `java gitlet.Main add hello.txt`
5. `java gitlet.Main commit "modify hello.txt"`

3中创建空的hello文本而5中将“hello”写入hello文本，上述行为log信息如下

```
===
commit 7cc2ab4320b35b2ffb4887220fc2f218d558c82f
Date: 13:08:36 UTC, Thursday, 11 April 2024
modify hello.txt

===
commit 4e07822b9c592f2c674752c14dcbbf59e70d3b67
Date: 13:08:13 UTC, Thursday, 11 April 2024
create hello.txt

===
commit 18a5bd36d2c406642225e9407d62ae6e20cf0bcf
Date: 00:00:00 UTC, Thursday, 1 January 1970
Init commit.
```

现在我们测试将hello文件恢复到46155...的提交中:

```
预期hello.txt内容：<空>
```

==测试没通过，发现commit有点问题，进行下一次commit时竟然会修改上一次commit的信息？？==

排查了半天发现是生成Commit.tree时有问题，我直接对Blobs对象调用toString，然后计算sha-1结果作为tree的值，但发现这样导致了两次提交同一个文件时会得到相同的sha-1哈希值（即指向相同的文件树）。

重新为Blobs类写了toString方法，现在正常工作了。



#### (2) 异常检测测试

- [x] commit id不存在时打印错误消息：
- [x] filename不存在或者不存在于id所指向的commit时，打印错误消息：



## 0412：branch、checkout分支、status、rm-branch

### branch [branchname]

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`

现在看一下.gitlet目录情况：

```
│  HEAD
│
├─objects
│  └─02
│          80d59f93980b3189ac7750285f49166392907a
│
└─refs
    └─heads
            master
```

即默认情况下，初始化后我们位于分支master。

现在创建新的分支test：`java gitlet.Main branch test`，再次查看.gitlet目录情况：

```
│  HEAD
│
├─objects
│  └─02
│          80d59f93980b3189ac7750285f49166392907a
│
└─refs
    └─heads
            master
            test
```



#### (2) 异常检测测试

- [x] 当创建的分支已存在时，输出异常并退出。

### checkout [branchname]

#### (1) 基本功能测试

继续在branch中测试的情况，即我们执行了：

1. `java gitlet.Main init`
2. `java gitlet.Main branch test`

现在checkout到test分支，没有什么变化，我们继续新建一个文本文件"hello.txt"，add并提交，查看log信息：

```
===
commit fe51d437c053a39bdfb39c8b26348959f42f34c7
Date: Fri Apr 12 03:52:56 2024 +0000
test: create hello.txt

===
commit 0280d59f93980b3189ac7750285f49166392907a
Date: Thu Jan 01 00:00:00 1970 +0000
initial commit

```

现在回到master分支：

```
预期：观察到工作目录中hello.txt已经被删除
实际：符合预期
```

再次查看log信息：

```
===
commit 0280d59f93980b3189ac7750285f49166392907a
Date: Thu Jan 01 00:00:00 1970 +0000
initial commit

```

可以看到只输出了初始提交，如果我们查看global-log：

```
===
commit 0280d59f93980b3189ac7750285f49166392907a
Date: Thu Jan 01 00:00:00 1970 +0000
initial commit

===
commit fe51d437c053a39bdfb39c8b26348959f42f34c7
Date: Fri Apr 12 03:52:56 2024 +0000
test: create hello.txt

```

可以看到我们在test分支执行的提交。如果继续checkout回到test分支，可以看到hello.txt重新出现。



#### (2) 异常检测测试

- [x] 要签出的分支不存在
- [x] 要签出的分支即当前分支
- [x] 当前工作目录中存在未追踪的文件



### status

工作目录状态。

#### (1) 基本功能测试

执行：

1. `java gitlet.Main init`
2. ``java gitlet.Main branch other-branch`
3. `java gitlet.Main add wug.txt`
4. `java gitlet.Main add wug2.txt`
5. `java gitlet.Main add wug3.txt`
6. `java gitlet.Main add junk.txt`
7. `java gitlet.Main add goodbye.txt`
8. `java gitlet.Main commit "first commit"`

现在我们分别：

+ 修改wug，wug2，wug3，但只执行add wug和add wug2 $\rightarrow$ wug和wug2出现在add暂存区，wug3是未暂存的modified。
+ 通过rm goodbye.txt删除goodbye文件 $\rightarrow$ goodbye出现在remove暂存区
+ 手动删除junk.txt $\rightarrow$ junk.txt是未暂存的deleted
+ 新建random.txt但并不执行add暂存 $\rightarrow$ random是untracked，因为它并没有被我们的第一次提交所追踪。



做完上述工作 我们可以执行add看看效果：

```
预期输出：
branch: *master, other-branch
staged: wug.txt wug2.txt
removed: goodbye.txt
modified but not staged: wug3(modified) junk.txt(removed)
untracked: random.txt
实际输出：
=== Branches ===
*master
other-branch

=== Staged Files ===
wug.txt
wug2.txt

=== Removed Files ===
goodbye.txt

=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)

=== Untracked Files ===
random.txt

```

输出符合我们的预期。

#### (2) 异常检测测试

无异常检测。



### rm-branch

删除分支。比较简单，直接在.gitlet/refs/heads目录下删除分支名所对应的文件即可。就不写测试记录了。

#### (1) 基本功能测试

通过

#### (2) 异常检测测试

- [x] 删除当前所处分支输出异常信息并退出；
- [x] 删除不存在的分支时输出异常消息并退出。



## 0413：完成reset和merge

写完没有自己进行测试，先提交了一次，拿了402.133/1600，下面进入debug流程。



## 0418：集成测试

（花了几天敷衍一下课题，今天继续debug）

翻到说明页面的下面才发现原来不用自己一个个输命令测试（天



集成测试脚本编写：

```
# ...  A comment, producing no effect.
I FILE Include.  Replace this statement with the contents of FILE,
      interpreted relative to the directory containing the .in file.
C DIR  Create, if necessary, and switch to a subdirectory named DIR under
      the main directory for this test.  If DIR is missing, changes
      back to the default directory.  This command is principally
      intended to let you set up remote repositories.
T N    Set the timeout for gitlet commands in the rest of this test to N
      seconds.
+ NAME F
      Copy the contents of src/F into a file named NAME.
- NAME
      Delete the file named NAME.
> COMMAND OPERANDS
LINE1
LINE2
...
<<<
      Run gitlet.Main with COMMAND ARGUMENTS as its parameters.  Compare
      its output with LINE1, LINE2, etc., reporting an error if there is
      "sufficient" discrepency.  The <<< delimiter may be followed by
      an asterisk (*), in which case, the preceding lines are treated as
      Python regular expressions and matched accordingly. The directory
      or JAR file containing the gitlet.Main program is assumed to be
      in directory DIR specifed by --progdir (default is ..).
= NAME F
      Check that the file named NAME is identical to src/F, and report an
      error if not.
* NAME
      Check that the file NAME does not exist, and report an error if it
      does.
E NAME
      Check that file or directory NAME exists, and report an error if it
      does not.
D VAR "VALUE"
      Defines the variable VAR to have the literal value VALUE.  VALUE is
      taken to be a raw Python string (as in r"VALUE").  Substitutions are
      first applied to VALUE.
```



举例：

```
# Add 2 files and commit with message "added 2 files"
> init
<<<
+ f.txt wug.txt
+ g.txt notwug.txt
> add g.txt
<<<
> add f.txt
<<<
> commit "added 2 files"
<<<
```



- [x] **add_status**：当新建文件并加入暂存区后，检查status，应当只有暂存区出现新建文件，untrack不必。

- [x] **remove_status**：提交后编译错误

- [x] **所有的err输出**：发现`System.exit()`中的值给错了，状态值应该给0而不是-1，所以所有的err输出都错了（

- [x] **remove-add-status**：假设删除一个当前已提交的跟踪文件f.txt，然后重新add，那么所显示的status为：

  ```
  === Branches ===
  *master
  
  === Staged Files ===
  
  === Removed Files ===
  
  === Modifications Not Staged For Commit ===
  
  === Untracked Files ===
  
  ```

  即相当于将f.txt从rm暂存区取消删除，而不用加入add暂存区。

- [x] **empty-commit-message-err**：提交空信息报错，忘记写了。
- [x] **nop-add**：如果add已经在头提交中追踪但没有任何更改的文件，不做处理。
- [x] **remove-deleted-file**：一开始写的时候没有考虑如果用户在执行rm之前已经将文件删除了该怎么办，所以报了错（。
- [x] **file-overwriteerror**：实际上是以头提交所在分支判断当前所处分支了，实际上这并不正确，因为当我们新建一个分支并不新建任何提交时，新分支和原分支指向同一个头提交，所以无法以提交中包含的分支来判断当前所处分支。



## 0420：继续debug

## merge-no-conflict

这个还是手动测试一下吧，集成测试没法控制过程。

步骤：

1. 初始化并提交两个文件：f.txt, g.txt；
2. 删除g.txt并新增h.txt，提交；
3. 新建other分支并切换；
4. 删除f.txt并新增k.txt，提交；
5. 切换回到master，执行merge other。

master合并后分支状态：f.txt与g.txt均被删除，同时保有h.txt和k.txt.

