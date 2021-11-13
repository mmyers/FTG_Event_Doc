@rem Batch file that I use to generate documentation for vanilla FTG in Windows 7.
@
java -jar ../dist/FTG_Event_Doc.jar -b "C:\Games\For the Glory" -f "Db\events.txt" -f "Scenarios\Inc\Reformation_Events.inc" -o "%USERPROFILE%\Documents\VanillaEvents" > output.txt
pause