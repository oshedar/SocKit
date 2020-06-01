package tictactoe;

public enum CellState {
    unmarked, //cell is unmarked
    x, // cell is marked with X
    o; // cell is marked with 0

    @Override
    public String toString() { 
       if(this == unmarked){
           return "u";
       }
       return this.name();
    }
}
