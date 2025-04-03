@rem Batch file that I use to check IDs in vanilla FTG
@
java -jar ../dist/FTG_Event_Doc.jar -b "C:\Games\For the Glory" -f "Db\events.txt" -f "Scenarios\Inc\Reformation_Events.inc" --only-check-ids > output.txt
pause