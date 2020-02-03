Issue reproduction: gradle-pitest vs Gradle test fixtures
====================

This repository reproduces an issue with the gradle-pitest plugin in Gradle
5.6+: applying Gradle's `java-test-fixtures` plugin makes pitest fail to
generate mutations. This seems to be a classpath issue.


Instructions
---

 1. Check out branch `working` (commit [`98744dfd`](https://github.com/emlun/issue-gradle-pitest-test-fixtures/commit/98744dfdef642125b33f509237f61031cde10e3e)):

    ```
    git checkout master
    ```

 2. Verify that pitest runs successfully:

    ```
    ./gradlew pitest
    [...]
    >> Generated 1 mutations Killed 1 (100%)
    >> Ran 1 tests (1 tests per mutation)
    ```

 3. Check out branch `broken` (commit [`cce47993`](https://github.com/emlun/issue-gradle-pitest-test-fixtures/commit/cce479937dcc712318eaa924e21d199452303f13)):

    ```
    git checkout broken
    ```

 4. Verify that pitest fails to generate any mutations:

    ```
    ./gradlew pitest
    [...]
    /1:07:49 PM PIT >> INFO : Calculated coverage in 0 seconds.
    1:07:49 PM PIT >> INFO : Created  0 mutation test units
    Exception in thread "main" org.pitest.help.PitHelpError: No mutations found. This probably means there is an issue with either the supplied classpath or filters.
    [...]
    ```


Additional info
---

Running Gradle with `--debug` shows a difference in class path between the two invocations:

- Without `java-test-fixtures` plugin:
  ```
  [INFO] [org.gradle.process.internal.DefaultExecHandle] Starting process 'command '/usr/lib/jvm/java-11-openjdk/bin/java''. Working directory: /home/emlun/dev/gradle-pitest-test-fixtures Command: /usr/lib/jvm/java-11-openjdk/bin/java -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp /home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest-command-line/1.4.9/9c3b0e1caa3520192b496087cd4432247fe47cb4/pitest-command-line-1.4.9.jar:/home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest-entry/1.4.9/3066c6594d074f49db755d45daec81d437346b00/pitest-entry-1.4.9.jar:/home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest/1.4.9/24cd1399f0d6e360a4552212e4b38c8da055cd32/pitest-1.4.9.jar org.pitest.mutationtest.commandline.MutationCoverageReport --reportDir=/home/emlun/dev/gradle-pitest-test-fixtures/build/reports/pitest --targetClasses=org.example.* --targetTests=org.example.* --sourceDirs=/home/emlun/dev/gradle-pitest-test-fixtures/src/main/resources,/home/emlun/dev/gradle-pitest-test-fixtures/src/main/java --mutableCodePaths=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/main --includeLaunchClasspath=false --classPath=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/main,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/main,/home/emlun/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar
  ```

- With `java-test-fixtures` plugin:
  ```
  [INFO] [org.gradle.process.internal.DefaultExecHandle] Starting process 'command '/usr/lib/jvm/java-11-openjdk/bin/java''. Working directory: /home/emlun/dev/gradle-pitest-test-fixtures Command: /usr/lib/jvm/java-11-openjdk/bin/java -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp /home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest-command-line/1.4.9/9c3b0e1caa3520192b496087cd4432247fe47cb4/pitest-command-line-1.4.9.jar:/home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest-entry/1.4.9/3066c6594d074f49db755d45daec81d437346b00/pitest-entry-1.4.9.jar:/home/emlun/.gradle/caches/modules-2/files-2.1/org.pitest/pitest/1.4.9/24cd1399f0d6e360a4552212e4b38c8da055cd32/pitest-1.4.9.jar org.pitest.mutationtest.commandline.MutationCoverageReport --reportDir=/home/emlun/dev/gradle-pitest-test-fixtures/build/reports/pitest --targetClasses=org.example.* --targetTests=org.example.* --sourceDirs=/home/emlun/dev/gradle-pitest-test-fixtures/src/main/resources,/home/emlun/dev/gradle-pitest-test-fixtures/src/main/java --mutableCodePaths=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/main --includeLaunchClasspath=false --classPath=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures-test-fixtures.jar,/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar
  ```

The difference is in the `--classPath` option:

```
--classPath=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/main,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/main,/home/emlun/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar

--classPath=/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/test,/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures-test-fixtures.jar,/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar,/home/emlun/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar
```

Specifically:

```
/home/emlun/dev/gradle-pitest-test-fixtures/build/classes/java/main,/home/emlun/dev/gradle-pitest-test-fixtures/build/resources/main

/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures-test-fixtures.jar,/home/emlun/dev/gradle-pitest-test-fixtures/build/libs/gradle-pitest-test-fixtures.jar
```
