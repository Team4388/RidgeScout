![Ridgescout](https://github.com/Team4388/ScoutingApp2025/blob/main/metadata/en-US/images/featureGraphic.png?raw=true)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.ridgebotics.ridgescout/)   
**Note**: The F-Droid version of this app is not currently up to date with the GitHub release

[**Read the wiki**](https://github.com/Team4388/ScoutingApp2025/wiki)

[**Test Data**](https://github.com/Team4388/ScoutingApp2025/blob/main/2024week0-1728149849985.scoutbundle)

#### Here is an overview of the main features currently included in the app:
- This project is written for Android! No need for some kind of janky laptop charging setup.
- Similar to ScoutingPASS, there are many diffrent types of fields that can be used to collect data.
- The app is designed to handle updates to the fields on the fly, without loosing any data!
- Unlike other scouting solutions, scouters can disable any field they did not measure, and disabled fields will not be included in any calculations.
- Dynamic displays based off of the diffrent fields.
- Data transfer including 2D codes, Bluetooth, and File Bundle.
- Exporting using CSV.
- Deployment on F-Droid
- Data cloud sync using an FTP server

#### Things that are yet to be implemented:
- A page that lets users cross-compare scouting data between teams. (Compare)
- A page that lets scouters more easily make reports to the drive team before a match starts (Report)

#### Things that may or may not be implemented:
- Statbotics intgration
- Scout error estimation using OPR-like calculation
- - Would most likely require Statbotics
https://www.thebluealliance.com/avatars
### Screenshots
|Match scouting interface|Field editor|Teams data viewer|
|-|-|-|
|![Screenshot1](https://github.com/Team4388/ScoutingApp2025/blob/main/metadata/en-US/images/phoneScreenshots/1.png?raw=true)|![Screenshot2](https://github.com/Team4388/ScoutingApp2025/blob/main/metadata/en-US/images/phoneScreenshots/2.png?raw=true)|![Screenshot3](https://github.com/Team4388/ScoutingApp2025/blob/main/metadata/en-US/images/phoneScreenshots/3.png?raw=true)|


<!--
Things:
- Added verbosity on could not find team number error
- Tally counter now has more visible font (@style/TextAppearance.MaterialComponents.Headline6)
- Match scouting title bar's background now fills its container (There was a small gap before)
- Added border around match title bar buttons
- Changed the rescout color to be a less obnoxious blue
- Removed blank space above pit scouting team selector
- Added new scouting indicator for pit scouting
- TBA now sometimes does not let you download all of the team images, go to https://www.thebluealliance.com/avatars in a web browser to try to fix this
- Moved dropdown title text box slightly farther up
- Added downwards pointing triangle to dropdown
- Removed slight gap in icon of the team option
- Add headers to settings
- Moved field edit buttons into the scroll view, so there is no overlap
-->