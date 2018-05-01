*********************************************************************************************************************************************************

	Project *** NHL PREDICTIONS

	By:		Pierre Seguin

*********************************************************************************************************************************************************


DATA SET
	NHL Playbyplay data must be downloaded for the project to work,
	You can download it using the following Scraper:
 	https://github.com/HarryShomer/Hockey-Scraper

	save the files as the following structure:
	./data/nhl_pbp{SEASON}.csv
	EX: ./data/nhl_pbp20172018.csv
	
	If you want to rebuild the classifier, you can run the GameCleaner class but be warned, this 
	takes a few hours.
		* A file will be saved as ./data/Parsed Game Data/games.txt
	The Fields are:
		Game_Id	Date	Period	Event	Description	Time_Elapsed	Seconds_Elapsed	Strength	Ev_Zone	Type	Ev_Team	Home_Zone	Away_Team	Home_Team	p1_name	p1_ID	
		p2_name	p2_ID	p3_name	p3_ID	awayPlayer1	awayPlayer1_id	awayPlayer2	awayPlayer2_id	awayPlayer3	awayPlayer3_id		awayPlayer4	awayPlayer4_id	awayPlayer5	
		awayPlayer5_id	awayPlayer6	awayPlayer6_id	homePlayer1	homePlayer1_id	homePlayer2	homePlayer2_id	homePlayer3		homePlayer3_id	homePlayer4	homePlayer4_id	
		homePlayer5	homePlayer5_id	homePlayer6	homePlayer6_id	Away_Players	Home_Players	Away_Score	Home_Score		Away_Goalie	Away_Goalie_Id	Home_Goalie	
		Home_Goalie_Id	xC	yC	Home_Coach	Away_Coach

	The game data can be reloaded by the Python script also present in the project, run it to 
	retrieve the newest game data
		* The files will be saved into ./data/Games/*
		* The naming convention is: {year-year}-Games.csv

	The Fields are:
		gamePk		link		gameType	season		gameDate	awayGamePoints	awaywins	awaylosses	awayties	awayid		awayname	
		awaylink	homeGamePoints	homewins	homelosses	hometies	homeid		homename	homelink




LOADING THE PROJECT
	Open eclipse and import the project
	Run Maven to install all dependencies

RUNNING THE PROJECT
	To start the program start the NHLGameRunner Main program and follow the instructions.

		Game Prediction:
			* Specify a date
			* Specify the home team and away Team
				Options are:
					MTL (Montreal Canadiens), 
					PIT (Pittsburgh Penguins), 
					WSH (Washington Capitals),
					NSH (Nashville Predators),
					TOR (Toronto Maple Leafs)...
				open the playby play to see them all
			* The Result will be the prediction of the team that won the game

		Season Prediction:
			* Enable / Disable the logger
			* Specify a date
			* The Result will be the ranking of all teams by the end of the season

*********************************************************************************************************************************************************

	WARNING
	This project has been tested on UBUNTU but not on any other operating system.

	If there is a Java Heap error, run the program with the VM config -Xmx1024M

*********************************************************************************************************************************************************

