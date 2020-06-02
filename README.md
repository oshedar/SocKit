![SocKit Logo](/logo.png)
#### SocKit is a complete solution to build multiplayer turn based games, removing the complexity of network programming and reducing your development time by providing standard turn based game features. With “SocKit” all you need to focus on is your game logic and design.

[Demo Poker Game](https://sockit.io/poker/index.html)

![Poker Demo](/poker.png)

### Overview
There are two java libraries (SocKitServer.jar and SocKitClient.jar) and one javascript client library. We have worked hard to name the classes and methods in a self explanatory way. We also provide a simple Tic Tac Toe game to get you started as well as a full featured poker game.

##### Begin on the server. Write your game logic. 
##### Then build your game client and connect it to the server

Once you have planned out your game. You should start your work on the server and write your game logic. The server library is a self contained web socket server. The library contains a default webpage handler and an embedded key value database, however you can plugin your own custom webpage handler and database.
On the server you start with the io.sockit.sockitserver package. You can read the server docs for more information about this package but here is a simple way to think about the main classes.

>The server is where you start. The server contains games and users. Games contain locations which contain rooms and rooms contain players. User logins create sessions and sessions become players when they join a room and take a seat

Once you have completed your game logic on the server. You can test your game using the SocKit Client API's. We have provided a java (Android) and javaScript library to connect to the server.

The Client connects to the server and joins a room. Room consists of Players. A Player represents a client that is seated in the Room. You need to define your own Client Event Listener which will receive events from the server.

### Documentation
[Read the Documentation](https://sockit.io/docs/index.html)