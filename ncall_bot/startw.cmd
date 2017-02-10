:: 이것을 작업관리자에 등록하면 윈도우 서비스처럼 구동이 가능함.<단, 경로를 적절히 수정>
start /b javaw -Dfile.encoding=UTF-8 -classpath "C:\Users\Taebu\git\ncallbot\ncall_bot\bin;C:\Users\Taebu\git\ncallbot\ncall_bot\libs\mysql-connector-java-5.1.35.jar;C:\Users\Taebu\git\ncallbot\ncall_bot\libs\log4j-1.2.17.jar;C:\Users\Taebu\git\ncallbot\ncall_bot\libs\json_simple-1.1.jar" kr.co.cashq.ncall_bot.NCALL_LOG
set /p val=enter please