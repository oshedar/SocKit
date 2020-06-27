/**
 * <p>
 * This is the core package for the Sockit Multiplayer Game (Server) Engine and provides the classes necessary to implement the server side of a multiplayer turn based game. The game Logic will reside on the server. Clients will only input events and render the game. 
 * To create/develop a game using this library the following 3 custom classes will have to be defined by the developer   
 * <p>
 * <b>Game Room</b>  This class will contain the Game Logic. The class should extend the Room class and override the various event/callback methods such as <code>onRoomJoined()</code>, <code>onSeatTaken()</code>, <code>beforeGameStarted()</code>, etc.
 * <p>
 * <b>Game Player</b> This class will maintain the player state. The class should extend the Player class. An instance of this class represents a single player in a room. 
 * <p>
 * <b>Game</b> This class is responsible for creation of Locations and add Game Room instances to each location. This class should extend the AbstractGame class
 * <p> </p>
 * <p> </p>
 * <b>Starting the Server</b> The Server class is used to Start and Stop the Game Engine. See example code
 * <br>
 * <pre>{@code
 *       Server.setDataStore(new LevelDbStore("./gameDb")); //sets the Database
 *       Server.setLogger(new FileLogger("./serverLog.txt",true)); // Sets the Logger
 *       Server.setWebHandler((new DefaultWebHandler("/webRoot"));  //Sets the web handler
 *       Server.registerGame(new TicTacToeGame());
 *       Server.startServerAsHttp(0); //0 will use default port of 80
 * } </pre>
*/
package io.sockit.sockitserver;
