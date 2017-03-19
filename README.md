# PSTrophies

Legacy repository of my second app. 
Please note that architecture, coding style, usage of libraries etc. is heavily outdated and doesn't reflect my today's 
knowledge.

The app provided guides how to get so called *trophies* in PlayStation 4 games. Trophies are some kind of achievements you 
can earn when playing a game. While some of them are earnable by just playing the game, some are difficult to get and people
often look at these guides. The app was only live from 07/2014 to 05/2015, as it consumed a lot of time to keep the app up to date.

* The app not only provided a guide for a trophy, but also made it easy to perform a Google and YouTube search or directly linked
to a YouTube video, which could be watched with an integrated player.
* The app got it's data from a MySQL database. The app connected to it via several PHP scripts and translated the data to
a local SQLite database. This way the database could be updated without releasing a new version.
* Features like favorites, prioritization of trophies (often lists of more than 30) via swiping and search.

<img src="https://github.com/TheSyex/PSTrophies/blob/master/Screenshots/screen1.png" width="270" height="480"> <img src="https://github.com/TheSyex/PSTrophies/blob/master/Screenshots/screen2.png" width="270" height="480">
<img src="https://github.com/TheSyex/PSTrophies/blob/master/Screenshots/screen3.png" width="270" height="480">
<img src="https://github.com/TheSyex/PSTrophies/blob/master/Screenshots/screen4.png" width="270" height="480">

### Try the app
I left a few games in the database to try the app. You can simply clone the repository and run the app, but be aware
that the app is in German only.
The games are:
* Alien Isolation
* Bloodborne
* Child of Light
* Destiny
* Escape Plan
* The Crew

You'll find them via the search or the listing.
