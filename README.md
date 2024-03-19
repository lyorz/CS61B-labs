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

