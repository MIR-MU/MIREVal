@echo off 
SETLOCAL ENABLEDELAYEDEXPANSION

set TOPICS=d:\skola\mir\SIGIR-2015\NTCIR11-Math2-queries-participants.xml
set QRELS=d:\skola\mir\SIGIR-2015\NTCIR11_Math-qrels.dat
set OUTPUT_DIR=d:\skola\mir\projects\output\
set RUN_NAME=MIRMU_CMath 
set QUERY_URL=https://mir.fi.muni.cz/webmias-ntcir-or

set "EXECUTABLE=java -cp d:\skola\mir\projects\MIREVal\target\MIREVal-1.0-SNAPSHOT.jar "

rem Next command only gets results of MIaS for the specified topics
set CMD_LINE_ARGS=cz.muni.fi.mireval.query.QueryApp -queryurl %QUERY_URL% -runname %1 -topics %TOPICS% -outputdir %OUTPUT_DIR%

rem Next command gets results of MIaS for the specified topics and evaluates them
rem set CMD_LINE_ARGS=cz.muni.fi.mireval.query.QueryApp -queryurl %QUERY_URL% -runname %1 -topics %TOPICS% -outputdir %OUTPUT_DIR% -qrels d:\skola\mir\SIGIR-2015\NTCIR11_Math-qrels.dat

rem Next command evaluates the specified tsv file according to specified qrels file
rem set CMD_LINE_ARGS=cz.muni.fi.mireval.EvalApp -tsvfile %1 -outputfile %2 -outputdir d:\skola\mir\projects\output\ -qrels d:\skola\mir\SIGIR-2015\NTCIR11_Math-qrels.dat

set CMD="%EXECUTABLE%%CMD_LINE_ARGS%"
call %EXECUTABLE% %CMD_LINE_ARGS%