/**
 * <p>
 * This is the core package for the Sockit Game Bot and provides the classes necessary to create bots running on the server side of a multiplayer turn based game.
 * Below are some of the core classes in this package  
 * <p>
 * <b>Bot</b>  This class represents a Bot running on the server. It has methods to login the Bot to the Server, enter a game, fetch list of rooms, join a room, etc. Bots are instantiated in the setUpGame() method of the Game class
 * <p>
 * <b>BotListener</b> Interface for Bot events/callbacks. An instance of this interface should be registered with the bot object to handle async events received from the server
 * <br>
 * <pre>{@code
 *      public class TicTacToeBotListener implements BotListener {
 *          {@literal @}Override
 *          public void onLoggedIn(Bot bot) {
 *              bot.getRooms( "Mumbai", RoomType.normal); //fetches room list from Mumbai location
 *          }
 * 
 *          {@literal @}Override
 *          public void onGetRooms(Bot bot, String gameName, String location, RoomType roomtype, List&lt;RoomInfo&gt; rooms) {
 *              bot.joinRoom(rooms.get(0).roomId); //joins the first room in the list
 *          }
 *          ....
 *      }
 * } </pre>
 * <p>
 * <b>Room</b> This class represents a Game Room. An instance of this class is passed to many of the event/callback methods in the BotListener interface
 * <p>
 * <b>Player</b> This class represents a Player seated in the room for a Bot
 */
package io.sockit.sockitserver.bot;
