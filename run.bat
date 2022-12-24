cd example
if "%1" equ "run" goto run
goto default

:run
chcp 65001
set NO_PROXY=localhost,127.0.0.1
flutter run -v
goto end

:default
adb devices
START /HIGH ..\run.bat run
goto end

:end
cd ..