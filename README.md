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

