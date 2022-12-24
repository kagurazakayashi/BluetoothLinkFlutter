cd example
if "%1" equ "apk" goto buildapk
goto default

:buildapk
chcp 65001
flutter build %1 -v
goto end

:default
START /HIGH ..\build.bat apk
goto end

:end
cd ..
