REM Option 2: Find what’s using 5433 and shut it down
REM Use this to check PID 2548 or 7756:

REM cmd
REM Copy
REM Edit

call show_ports.bat

echo Try somethign like this:
echo tasklist /FI "PID eq 2548"
echo tasklist /FI "PID eq 7756"