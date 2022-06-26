echo off

rem  Copyright 2009 Giles Burgess
rem
rem  Licensed under the Apache License, Version 2.0 (the "License");
rem  you may not use this file except in compliance with the License.
rem  You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem  Unless required by applicable law or agreed to in writing, software
rem  distributed under the License is distributed on an "AS IS" BASIS,
rem  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem  See the License for the specific language governing permissions and
rem  limitations under the License.

echo.
setlocal
set ANT_JAR=%ANT_HOME%\lib\ant.jar
set TOOLS=%JAVA_HOME%\lib\tools.jar
set DIR=build
set CLASS=Make
set ARGS=

if not exist %ANT_JAR% (
  echo %%ANT_HOME%%\lib\ant.jar not found
  goto end
)

if not exist %TOOLS% (
  echo %%JAVA_HOME%%\lib\tools.jar not found
  goto end
)

if not ""%1""==""--build"" goto setupArgs
shift
set CLASS=%1
shift

:setupArgs
if ""%1""=="""" goto doneArgs
set ARGS=%ARGS% %1
shift
goto setupArgs

:doneArgs
set FILE=%CLASS:.=/%
if exist "%DIR%/%FILE%.java" goto begin
echo Missing source file: %DIR%\%CLASS%.java
endlocal
exit/b 1

:begin
set DEFS=-Dmake.java.dir=%DIR%
set CP=%DIR%;%DIR%/*;%ANT_JAR%;%TOOLS%

javac -cp %CP% %DIR%/%FILE%.java
java -cp %CP% %DEFS% %CLASS% %ARGS%
del/s %DIR%\*.class >nul

:end
endlocal
