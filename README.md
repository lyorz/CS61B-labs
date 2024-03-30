## Lab3
>2024/03/12

提交至gradescope后编译出错：

```
Compiling tests for TimingTest... 
=========== COMPILATION ERROR =============
/autograder/submission/lab3/./timingtest/TimeAList.java:3: error: package org.checkerframework.checker.units.qual does not exist
import org.checkerframework.checker.units.qual.A;
                                              ^
/autograder/submission/lab3/./timingtest/TimeSLList.java:3: error: package org.checkerframework.checker.units.qual does not exist
import org.checkerframework.checker.units.qual.A;
                                              ^
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
2 errors
Compiling tests for RandomizedTest... 
success.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
```

感觉应该不是我的问题（

> 2024/03/13

原来是import的问题，删除编译错误中的非必要import即可。

## Proj1: CheckPoint
> 2024/03/13

16.0/16.0通过。

## Proj1: DataStructure
> 2024/03/18

断断续续写了好几天，提交了也有七八遍了终于过了autograder所有的test，虽然格式扣掉了96分，最后544/640（

### LinkedListDeque
链表形式的双端队列还是好写的，注意在Add、Remove过程中维护好指向前后节点的指针即可。

### ArrayDeque
ArrayDeque真的难。

基本结构用的是讲义里推荐的循环数组，这个确实很绕，写的时候得用草稿纸画画才能明白。

我用两个指针维护队列首尾（start和end）增删、以及扩容缩容都需要考虑周全首尾指针的位置关系。

虽然过程中不断调试修改，但最后确实收获很多，蛮有成就感的。

## Proj1EC: Autograding
> 2024/03/19

始终过不了ag，遂弃。
## Lab4: Git
> 2024/03/20

由于课程已经完结了，所有项目文件都是直接clone下来的，助教当时设置的合并冲突不能通过pull出现了，只能想办法回溯到过去的状态。

所以新建一个分支回到pull lab4之前的状态：
```
git checkout dd477f37b7c97f22868aa7482e2391fbced60bf6
git branch onlyforlab4
git checkout onlyforlab4
```
注意我们此时的lab1是没有做过的状态，所以需要在新的onlyforlab4分支上重新写一遍lab1/Collatz.java并提交。


现在我们可以通过`git pull skeleton master`并出现合并冲突了，修正合并冲突后add并commit，并将我们新创建的分支推送到github：

```
git push --set-upstream origin onlyforlab4
```


之后就可以在lab4A的自动评分器提交我们的onlyforlab4分支了。


lab4b比较简单，直接git log查看正确提交lab1/Collatz.java的哈希值，并将lab1/Collatz.java回溯到正确的状态：
```
git checkout 5a9c9d1893d8e9d3973aaf0cdf141964f1c71e71 -- lab1/Collatz.java
```


然后正常add、commit、push即可提交到Lab4B的autograder，注意此时我们需要push到新建的分支`git push origin onlyforlab4`



做Lab4调试作业的时候发现一个之前没太注意的问题，在Java中，当我们对Integer对象使用==运算符时将比较两个对象的引用是否相同而不是比较它们的值是否相等。


**但为什么从0开始遍历，只在128中出现了错误呢？**


**答案是**：在Java中，Integer 类中的静态方法 valueOf 会返回一个缓存中的对象，如果数值在 -128 到 127 之间，则会返回缓存中的对象，否则会创建一个新的对象。因此，当你使用 == 运算符比较两个数值在范围内的 Integer 对象时，它们可能会返回 true，但当比较超出范围的整数时，它们会返回 false。所以最后我们使用equals方法来比较两个Integer对象的值是否相等。

# Lab6: Getting Started on Project 2
>2024/03/24

Lab5是Peer Code Reviewer没法做。Lab6比较简单，但是对认识git的存储原理有很大帮助。


# Lab7: BSTMap
> 2024/03/30

扣了12.8分，因为感觉完成remove(K key, V value)需要对V类型数据进行比较，那么就要使用compareTo，所以对V类型也进行了扩展，但这样造成接口错误（

>Testing bstmap.BSTMap.java
>Wrong type paramters: 
>  *  Expected: <K extends Comparable<K>,V>
>  *  Received: <K extends Comparable<K>,V extends Comparable<V>>
