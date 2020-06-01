/**
 *  <div class="hidejava">
 * <p>
 * This is the core package for the Sockit Game Client and provides the classes necessary to implement the client (front end) side of a multiplayer game side of a multiplayer turn based game. Clients should only input user events/actions and render the game.
 * Below are some of the core classes in this package  
 * <p>
 * <b>Client</b>  This class represents a Client interacting with the server. It has methods to login a User to the Server, enter a game, fetch list of rooms, join a room, etc. See startup code example below 
 * <br>
 * <pre>{@code
 *      // instantiate a client with the server url
 *       Client client = new Client("ws://localhost"); 
 *      // register a ClientEventListener (event/callback handler) with the client
 *       client.setClientEventListener(new TicTacToeClientListener());
 *      // register user and login to the server with emailId, password, user name and game name as parameters
 *       client.registerWithEmailId("a@a.com", "123", "Rohan", "TicTacToe");
 * } </pre>
 * <p>
 * <b>ClientEventListener</b> Interface for Client events/callbacks. An instance of this interface should be registered with the client object to handle async events received from the server
 * <br>
 * <pre>{@code
 *      public class TicTacToeClientListener implements ClientEventListener {
 *          {@literal @}Override
 *          public void onLoggedIn(Client client, boolean isGameSelected) {
 *              if(isGameSelected)
 *                  client.getRooms( "Mumbai", RoomType.normal); //fetches room list from Mumbai location
 *          }
 * 
 *          {@literal @}Override
 *          public void onGetRooms(Client client, String gameName, String location, RoomType roomtype, List<RoomInfo> rooms) {
 *              client.joinRoom(rooms.get(0).roomId); //joins the first room in the list
 *          }
 *         //....
 *      }
 * } </pre>
 * <p>
 * <b>Room</b> This class represents a Game Room. An instance of this class is passed to many of the event/callback methods and can be used to render the room and game play onto the UI
 * <p>
 * <b>Player</b> This class represents a Player seated in the room and can be used to render a player onto the UI
 * </div>
 * 
 * <div class="javascript" style="display:none;">
 * This API provides the javascript Object/Function prototypes necessary to implement the client (front end) side of a multiplayer game side of a multiplayer turn based game. Clients should only input user events/actions and render the game. Below are 
 * Below are some of the core Object/Function prototypes  in this library  
 * <p>
 * <b>Client</b>  This Object/Function prototype represents a Client interacting with the server. It has methods to login a User to the Server, enter a game, fetch list of rooms, join a room, etc. See startup code example below 
 * <br>
 * <pre>{@code
 *      // instantiate a client with the server url
 *       var client = new Client("ws://"+document.location.hostname);
 *      // register a ClientEventListener (event/callback handler) with the client
 *       client.setClientEventListener(new TicTacToeClientListener());
 *      // register user and login to the server with emailId, password, user name and game name as parameters
 *       client.registerWithEmailId("a@a.com", "123", "Rohan", "TicTacToe");
 * } </pre>
 * <p>
 * <b>ClientEventListener</b> This is a custom user defined object with one or more of the  Client event/calback methods defined. This object should be registered with the client to handle async events received from the server
 * <br>
 * <pre>{@code
 *      function TicTacToeClientListener() {
 *          this.onLoggedIn=function(client, isGameSelected) {
 *              if(isGameSelected)
 *                  client.getRooms( "Mumbai", RoomType.normal); //fetches room list from Mumbai location
 *          };
 * 
 *          this.onGetRooms=function(client, gameName, location, roomtype, rooms) {
 *              client.joinRoom(rooms.get(0).roomId); //joins the first room in the list
 *          };
 *         //....
 *      }
 * } </pre>
 * <p>
 * <b>Room</b> This Object/Function prototype represents a Game Room. An object of this prototype is passed to many of the event/callback methods and can be used to render the room and game play onto the UI
 * <p>
 * <b>Player</b> This Object/Function prototype represents a Player seated in the room and can be used to render a player onto the UI
 * </div>
 */
package io.sockit.gameclient;
