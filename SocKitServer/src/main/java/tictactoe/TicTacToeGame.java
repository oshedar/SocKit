package tictactoe;

import javax.json.JsonObject;
import io.sockit.sockitserver.CompressibleData;
import io.sockit.sockitserver.Game;
import io.sockit.sockitserver.Room;
import io.sockit.sockitserver.RoomType;
import io.sockit.sockitserver.Session;
import io.sockit.sockitserver.Location;

public class TicTacToeGame extends Game{

    public TicTacToeGame() {
        super("TicTacToe", true);
    }
    

    @Override
    protected void setUpGame() {
        //add a location to the game
       this.addNewLocation("Mumbai");
       //add rooms for each location
       for(Location location: this.getLocations()){
           location.addRoom(new TicTacToeRoom(this, "Room 1", RoomType.normal, 60, 8, false));
           location.addRoom(new TicTacToeRoom(this, "Room 2", RoomType.normal, 60, 8, false));
           location.addRoom(new TicTacToeRoom(this, "Room 3", RoomType.normal, 60, 8, false));
       }
    }

    @Override
    protected Room newRoom(String roomName, RoomType roomType, int seatCount, int turnDurationInSecs, JsonObject additionalRoomConfig) {
        return new TicTacToeRoom(this, roomName, roomType, seatCount, turnDurationInSecs, false);
    }

    @Override
    protected CompressibleData newGameUserData() {
        return null;
    }


    @Override
    protected void onEnterGame(Session sn, boolean bln) {
        
    }

    @Override
    protected void onExitGame(Session sn) {
        
    }
}
