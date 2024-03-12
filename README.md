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
