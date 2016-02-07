iTunes Juke Box is a Java application designed to run a party from your
Mac by driving the iTunes via Applescript.  The application starts up a
web-server (by default on port 8080), allows you to choose the party
playlist from iTune, and allows people to view the tracks available
and vote on their favorites.  The play list then gets updated dynamically
according to people's preferences. 

To get this working you can package it as a jar if you'd like by doing:

 1. mvn package
 
Run the application with:

 1. Run the jar with java -jar itunesjukebox.jar
 2. You can override properties using a '-p property-file' argument. 
 3. By default the web-server starts on port 8080 although that can be overridden
 4. Go to http://localhost:8080 which will encourage you to go to /admin/
 5. Enter the admin password for this run of the application.
 6. Choose a playlist to copy from into our temporary playlist.
 7. Get your IP address of your system.
 8. Encourage other folks to go to your webpage, see the tracks, and vote on them.
