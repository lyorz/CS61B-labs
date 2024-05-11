# Gitlet Design Document

**Name**: lyorz



## Classes and Data Structures

### Main

接收命令行参数，根据命令（`arg[0]`）调用相应的命令。

#### Fields

没有字段，本类只验证参数并执行相应命令。

### Repository

#### Fileds

1. `static final File CWD = new File(System.getProperty("user.dir"))`

   当前工作目录。

2. `static final File GITLET_DIR = join(CWD, ".gitlet")`

   隐藏目录`.gitlet/`。本项目中存储所有状态的地方，对package中其他类公开，因此其他类也可以使用它来保存状态。
   
3. `public static final File OBJECTS_DIR`

   目录`.gitlet/objects/`。本项目中所有对象的存储位置，包含文件内容、commit对象等。

4. `public static final File REFS_DIR`

   目录`.gitlet/refs/`。包含heads目录（参照git设置的，但是感觉放在本项目中有一些多余）

5. `public static final File HEADS_DIR`

   目录`.gitlet/refs/heads/`，目录下存放所有分支，文件名以分支名命名，其中以String类型存放了该分支对应头提交的sha1值。

6. `public static final File INDEX`

   文件`.gitlet/index`，存放暂存区内容，以blobs对象表示。

7. `public static final File HEAD`

   文件`.gitlet/HEAD`，存放当前头提交，以File对象表示，读取之后可以获得`.gitlet/refs/head/<分支>`对象。

### Commit

#### Fileds

1. `String message`：维护提交的消息

1. `String branch`：当前提交所处分支名（但有时候并不可靠，当两个分支指针指向同一个提交时，很难区分）；

2. `String timestamp`：创建提交的时间戳。由构造函数指定。

6. `String parent`：当前提交的父提交ID。

6. `String secondParent`：当前提交的第二父提交ID（*仅为merge得到的提交所拥有，其他情况下的普通提交该字段为null*）。

5. `String ID`：由其余字段经过sha1 hash生成的唯一标识符，可据此区分不同的提交。HEAD也据此来记录当前所处位置。

6. `String tree`：sha1-hash值，根据该值可以追踪到存储当前提交所保存文件的快照，快照中遵循格式

   ```
   blob1{<filename>, <sha-1 hash>}
   ...
   blobn{<filename>, <sha-1 hash>}
   ```

   保存多个blob对象。

#### Methods

**私有函数**

暂无

**公共函数**

| 函数                                                         | 功能                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| public Commit(String message)                                | 构造函数，接收message，实例化Commit对象（由于默认初始提交message为“init commit”所以可以区分初始提交和其他提交）。 |
| public Commit(Commit currHead, Commit givenHead)             | 构造函数，接收当前分支的头提交、给定分支的头提交，本函数特定为merge行为实例化Commit对象。 |
| public boolean equals(Object obj)                            | override父类equals函数，用于Commit对象间比较，若对象相同则返回true |
| public boolean isMergedCommit()                              | 返回当前提交是否为Merge的结果。                              |
| public String getBranch()                                    | 返回当前提交所处分支。                                       |
| public static Commit readCommitWithID(String commitID)       | 返回给定ID对应的Commit对象。                                 |
| public static Commit fromfile(File f)                        | 从输入文件f中读取Commit对象                                  |
| public void saveCommit()                                     | 将Commit对象保存在文件中（路径为：.gitlet/objects/ID[:2]/ID[2:]） |
| public Commit getParent()                                    | 返回当前提交的父提交节点（如果本身是初始提交则返回null）     |
| public Commit getSecondParent()                              | 返回当前提交的第二父提交节点（如果不是merge提交则返回null）  |
| public String getLog()                                       | 返回当前提交的Log信息，输出统一格式的字符串。                |
| public Blobs getTrackingTree()                               | 返回当前提交的追踪文件树，以Blobs类型。                      |
| public Blobs getBlobsofTree()                                | 返回新提交追踪文件列表的Blobs对象。（同时考虑当前提交所追踪的文件，并根据暂存区进行增减。） |
| public void saveTree(Blobs tree)                             | 保存当前提交追踪文件（保存路径为：.gitlet/objects/tree[:2]/tree[2:]） |
| public String sameMessage(String message)                    | 返回提交ID，若给定信息和当前提交维护信息相等，否则返回null   |
| public static TreeMap<String, ArrayList<String>> getCommitGraph() | 返回全局所有提交构成的图，以邻接表结构描述。                 |



### Blobs

主要实现两个功能：

1. 描述暂存区状态：addStagingArea、rmStagingArea；
2. 描述提交所追踪的文件树：所有文件将存储于addStagingArea，rmStagingArea为空。

#### Fileds

1. `TreeMap<String, String> addStagingArea`：添加文件暂存区

2. `TreeMap<String, String> rmStagingArea`：删除文件暂存区

3. `class blob`：为什么创建了这样一个成员？是因为TreeMap似乎无法直接通过Utils.writeObject写入文件，所以考虑为其创建一个能够序列化的子类方便对象的读写。

   > 看起来很怪但我暂时没想到更好的方法。

   + `String operation`：外部对该文件做出的操作：add/rm
   + `String filename`：文件名
   + `String ID`：根据filename指向文件内容计算出的sha-1哈希值。
   + `blob(String filename, String ID, String operation)`：blob对象构造函数

#### Methods

**私有函数**

| 函数                                                         | 功能                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| private void fromfile(File f)                                | 从文件f中读取暂存区/提交追踪文件树内容                       |
| private void put(String filename, TreeMap<String, String> StagingArea) | 根据外部调用操作（add/rm）将filename放入暂存区。（如果已在暂存区中则替换其内容） |

**公共函数**

| 函数                                                         | 功能                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| public Blobs()                                               | 实例化空暂存区                                               |
| public Blobs(File f)                                         | 从文件f读取Blobs对象                                         |
| public boolean isEmpty()                                     | 判断暂存区是否为空                                           |
| public void saveBlobs(File f)                                | 将暂存区/提交追踪文件树内容写入文件f                         |
| public void operateStagingArea(String filename, String operation) | 根据输入operation将filename添加进入add/rm暂存区              |
| public boolean find(String key)                              | 查找暂存区中是否存在键为key的条目                            |
| public void remove(String key)                               | 将键为key的条目从暂存区中移除                                |
| public void checkforCommit(Blobs TrackingTree)               | 执行commit时检查新提交所追踪内容，输入TrackingTree为当前提交追踪文件 |
| public String toString()                                     | 返回Blobs对象字符串形式，用于存储Blobs对象                   |
| public TreeMap<String, String> getAddedFiles()               | 返回add暂存区，以哈希结构                                    |
| public TreeMap<String, String> getRemovedFiles()             | 返回rm暂存区，以哈希结构                                     |



### Command

没有任何字段，该类由一系列函数组成，这些函数根据相应指令做出动作。

#### 私有函数

| 函数                                    | 功能                   |
| --------------------------------------- | ---------------------- |
| boolean branchExists(String branchName) | 返回给定分支名是否存在 |



#### 公有函数

| 函数                                         | 功能                                                         |
| -------------------------------------------- | ------------------------------------------------------------ |
| init()                                       | 初始化空git仓库                                              |
| add(String addFile)                          | 将给定文件addFile加入暂存区                                  |
| commit(String message)                       | 根据给定message新建提交                                      |
| log()                                        | 打印当前分支提交信息                                         |
| rm(String rmfilename)                        | 删除文件，如果文件已暂存则取消；若未暂存则删除并加入rm暂存区 |
| find(String message)                         | 寻找给定信息所对应的提交                                     |
| checkoutResetfile(String filename)           | checkout  -- [filename] 将指定文件更新到头提交对应的版本     |
| checkoutLastfile(String ID, String filename) | checkout [commit id] -- [filename] 将指定文件更新到给定提交对应的版本 |
| checkout(String branchName)                  | 切换分支                                                     |
| branch(String branchName)                    | 新建分支                                                     |
| status()                                     | 显示当前状态                                                 |
| reset(String commitID)                       | 更新状态到给定提交                                           |
| merge(String branchName)                     | 将当前分支和给定分支混合                                     |



## Algorithms

## Persistence

```
.gitlet/
	- HEAD 			-- 指向当前git branch路径（存储File类型对象）
	- refs/
		- heads/ 	-- 记录所有git branch文件，文件内容为branch指针当前所指向的commit哈希								值（在我们的实现中，即Commit对象的ID字段）
	- objects/ 		-- 维护所有：文件内容、commit信息、某一commit中的文件快照
	- index 		-- 维护暂存区状态，以Blobs对象格式
```



## Git目录结构理解

初始化一个空的git仓库，其目录结构如下：

```
│  config
│  description
│  HEAD
│
├─hooks
│      applypatch-msg.sample
│      commit-msg.sample
│      fsmonitor-watchman.sample
│      post-update.sample
│      pre-applypatch.sample
│      pre-commit.sample
│      pre-merge-commit.sample
│      pre-push.sample
│      pre-rebase.sample
│      pre-receive.sample
│      prepare-commit-msg.sample
│      push-to-checkout.sample
│      update.sample
│
├─info
│      exclude
│
├─objects
│  ├─info
│  └─pack
└─refs
    ├─heads
    └─tags
```

(发现一件有趣的事情，此时cat HEAD会发现它回显`ref: refs/heads/master`)，但是我们可以看到heads目录下其实此时是空的，尚且没有HEAD指针所指向的Commit ID，执行git log会提示我们在当前分支master上没有提交。



现在创建一个文本文件“helloGit.txt”并将它add到暂存区。再来看文件目录结构：

```
│  config
│  description
│  HEAD
│  index
│
├─hooks
│      applypatch-msg.sample
│      commit-msg.sample
│      fsmonitor-watchman.sample
│      post-update.sample
│      pre-applypatch.sample
│      pre-commit.sample
│      pre-merge-commit.sample
│      pre-push.sample
│      pre-rebase.sample
│      pre-receive.sample
│      prepare-commit-msg.sample
│      push-to-checkout.sample
│      update.sample
│
├─info
│      exclude
│
├─objects
│  ├─e6
│  │      9de29bb2d1d6434b8b29ae775ad8c2e48c5391
│  │
│  ├─info
│  └─pack
└─refs
    ├─heads
    └─tags
```

对比前后发现我们在.git目录中多出一个index文件，在objects文件夹下又多出了一个子目录：

+ index：用于维护add到暂存区的文件（包括创建、更改、删除）

  ```
  git ls-files --stage
  100644 e69de29bb2d1d6434b8b29ae775ad8c2e48c5391 0       helloGit.txt
  ```

  第二列是我们为helloGit.txt计算出的唯一标识符，我们称它为blob ID

+ objects：

  ```
  ├─objects
  │  ├─e6
  │  │      9de29bb2d1d6434b8b29ae775ad8c2e48c5391
  ```

  可以看到objects多出的子目录和其下文件的名称组合起来就是为helloGit.txt计算得到的blob ID。





现在我们提交暂存区内容`git commit -m "created helloGit.txt"`，查看文件目录结构变化：

```
│  COMMIT_EDITMSG
│  config
│  description
│  HEAD
│  index
│
├─hooks
│      applypatch-msg.sample
│      commit-msg.sample
│      fsmonitor-watchman.sample
│      post-update.sample
│      pre-applypatch.sample
│      pre-commit.sample
│      pre-merge-commit.sample
│      pre-push.sample
│      pre-rebase.sample
│      pre-receive.sample
│      prepare-commit-msg.sample
│      push-to-checkout.sample
│      update.sample
│
├─info
│      exclude
│
├─logs
│  │  HEAD
│  │
│  └─refs
│      └─heads
│              master
│
├─objects
│  ├─6c
│  │      717190a08ab455c0498faadeb9d7d9b302613b
│  │
│  ├─73
│  │      58fc2a549b3c322ceda3e9fcb290a2d3dd6f4e
│  │
│  ├─e6
│  │      9de29bb2d1d6434b8b29ae775ad8c2e48c5391
│  │
│  ├─info
│  └─pack
└─refs
    ├─heads
    │      master
    │
    └─tags
```



+ COMMIT_EDITMSG

+ refs/heads/master

+ logs:

+ objects:

  ```
  ├─objects
  │  ├─6c
  │  │      717190a08ab455c0498faadeb9d7d9b302613b	//当前提交下所有文件信息
  │  │
  │  ├─73
  │  │      58fc2a549b3c322ceda3e9fcb290a2d3dd6f4e	//HEAD指向的commit ID
  │  │
  │  ├─e6
  │  │      9de29bb2d1d6434b8b29ae775ad8c2e48c5391	//helloGit.txt文件内容
  ```

  现在详细解释这三个文件中各自包含什么信息，我们用sha1哈希值的前六位来表示该文件

  > 当我们需要显示.git所存储的文件信息时，需要执行命令：`git cat-file -p <filename>`

  `git cat-file -p 7358fc`

  ```
  tree 6c717190a08ab455c0498faadeb9d7d9b302613b
  author lyorz <lyorzorz@163.com> 1712561331 +0800
  committer lyorz <lyorzorz@163.com> 1712561331 +0800
  
  created helloGit.txt
  ```

  `git cat-file -p e69de2 `

  ```
  ```

  ` git cat-file -p 6c7171`

  ```
  100644 blob e69de29bb2d1d6434b8b29ae775ad8c2e48c5391    helloGit.txt
  ```

  容易知道7358fc保存的是我们的提交信息，注意到7358fc中的6c7171字段指向的内容维护了当前提交下的所有文件信息；而6c7171中又记录了文件对应的哈希散列值，我们可以根据e69de2找到对应的文件内容。



现在我们再次创建一个新的gitlet.txt文件，add并commit，这是提交之后的文件目录：

```
│  COMMIT_EDITMSG
│  config
│  description
│  HEAD
│  index
│
├─hooks
│      applypatch-msg.sample
│      commit-msg.sample
│      fsmonitor-watchman.sample
│      post-update.sample
│      pre-applypatch.sample
│      pre-commit.sample
│      pre-merge-commit.sample
│      pre-push.sample
│      pre-rebase.sample
│      pre-receive.sample
│      prepare-commit-msg.sample
│      push-to-checkout.sample
│      update.sample
│
├─info
│      exclude
│
├─logs
│  │  HEAD
│  │
│  └─refs
│      └─heads
│              master
│
├─objects
│  ├─6c
│  │      717190a08ab455c0498faadeb9d7d9b302613b
│  │
│  ├─73
│  │      58fc2a549b3c322ceda3e9fcb290a2d3dd6f4e
│  │
│  ├─79
│  │      75219e3ce3b1b98a4a659a49bab080fe8f98d3
│  │
│  ├─aa
│  │      0ac81b19014bb3b8b26808d493d9fc5862eba8
│  │
│  ├─e6
│  │      9de29bb2d1d6434b8b29ae775ad8c2e48c5391
│  │
│  ├─info
│  └─pack
└─refs
    ├─heads
    │      master
    │
    └─tags
```

可以看到objects目录下新增了文件：797521、aa0ac81，我们将内容展示出来：

`git cat-file -p aa0ac81`

```
100644 blob e69de29bb2d1d6434b8b29ae775ad8c2e48c5391    gitlet.txt
100644 blob e69de29bb2d1d6434b8b29ae775ad8c2e48c5391    helloGit.txt
```

根据输出可以知道这是一个新的因为这两个文本文件内容都是空的，所以它们都指向e69de2。

而多出来的757921则记录了当前commit的相关信息`git cat-file -p 797521`

```
tree aa0ac81b19014bb3b8b26808d493d9fc5862eba8
parent 7358fc2a549b3c322ceda3e9fcb290a2d3dd6f4e
author lyorz <lyorzorz@163.com> 1712562818 +0800
committer lyorz <lyorzorz@163.com> 1712562818 +0800
```

此外还要注意到我们多出了一个parent字段，它指向上一个提交（即7358fc）。
