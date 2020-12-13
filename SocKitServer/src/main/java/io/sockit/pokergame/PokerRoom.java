/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.pokergame;

import io.sockit.sockitserver.Player;
import io.sockit.sockitserver.Room;
import io.sockit.sockitserver.RoomType;
import io.sockit.sockitserver.Session;
import javax.json.JsonObject;
import io.sockit.sockitserver.User;
import io.sockit.servertools.ArrayIntList;
import io.sockit.sockitserver.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author h
 */
public class PokerRoom extends Room {
    
    private Deck deck=Deck.freshDeck();
    int dealerSeatNo;
    int smallBlindSeatNo;
    int bigBlindSeatNo;
    int smallBlind;
    int bigBlind;
    int maxBetInPot;//max player chipsInPot
    PokerRound bettingRound;
    List<Card> tableCards=new ArrayList(5);
    String prevGameWinnerNames="";
    String winningHandDescription="";
    
    PokerRoom(PokerGame roomFactory,String roomName, RoomType roomType, int maxNoOfPlayers,int turnDurationInSecs,int delayAfterGameEndedInSecs,int smallBlind,boolean enableDebug) {
        super(roomFactory, roomName,roomType,maxNoOfPlayers,turnDurationInSecs,delayAfterGameEndedInSecs,enableDebug);
        dealerSeatNo=maxNoOfPlayers;
        this.smallBlind=smallBlind;
        this.bigBlind=smallBlind*2;
    }
    
    void addChipsToPot(PokerPlayer player,int amt) throws InvalidBetException{
        if(amt>=player.chipsOnTable)
            amt=player.chipsOnTable;
        if(maxBetInPot>player.chipsInPot+amt){
            if(amt==0)
                throw new InvalidBetException(maxBetInPot-player.chipsInPot, amt);
        }
        if(amt==0){
            player.lastAction=PlayerAction.checked;
        }
        else if(player.chipsOnTable-amt==0)
            player.lastAction=PlayerAction.allIn;
        else if(player.chipsInPot+amt==maxBetInPot){
            player.lastAction=PlayerAction.called;
        }
        else{
            int callValue=maxBetInPot-player.chipsInPot;
            int minRaiseBet=callValue*2;
            if(minRaiseBet<callValue+this.bigBlind)
                minRaiseBet=callValue+this.bigBlind;
            if(amt<minRaiseBet)
                throw new InvalidBetException(minRaiseBet, amt);
            player.lastAction=PlayerAction.raised;            
        }

        player.chipsInPot +=amt;
        player.chipsOnTable-=amt;
        player.lastAmtBet=amt;
        if(player.chipsInPot>maxBetInPot){
            player.raisedBy=player.chipsInPot-maxBetInPot;
            maxBetInPot=player.chipsInPot;
        }
        calculatePots();
    }
    
    private ArrayIntList potMaxBets=new ArrayIntList(5);
    private ArrayIntList potValues=new ArrayIntList(5);
    void calculatePots(){
        potMaxBets.clear();
        potValues.clear();
        //fill maxbets 
        //iterate through all players inGame and add chipsinPot of allIn players 
        //but dont add duplicate values
        Iterable<Player> activePlayers=this.getActivePlayers();
        PokerPlayer pokerPlayer;
        for(Player player:activePlayers){
            pokerPlayer=(PokerPlayer)player;
            if(pokerPlayer.lastAction==PlayerAction.allIn){
                if(!potMaxBets.contains(pokerPlayer.chipsInPot))
                    potMaxBets.add(pokerPlayer.chipsInPot);
            }
        }
        if(!potMaxBets.contains(maxBetInPot))
            potMaxBets.add(maxBetInPot);
        potMaxBets.sort();
        //fill potValues
        //add as many elements as potMaxBets to potValues initialised to 0
        //then iterate through all players who played game and keep deducting (maxBet-prevMaxBet) from 
        //playerChipsInPot and adding to potValues
        int noOfPots=potMaxBets.size();
        for(int ctr=0;ctr<noOfPots;ctr++)
            potValues.add(0);
        int prevPotMaxBet;
        int potMaxBet;
        Iterable<Player> playersWhoPlayedGame=this.getPlayersWhoPlayed();
        for(Player player:playersWhoPlayedGame){
            pokerPlayer=(PokerPlayer)player;
            prevPotMaxBet=0;
            for(int ctr=0;ctr<noOfPots;ctr++){
                potMaxBet=potMaxBets.get(ctr);
                if(pokerPlayer.chipsInPot<potMaxBet){
                    potValues.set(ctr, potValues.get(ctr)+(pokerPlayer.chipsInPot-prevPotMaxBet));
                    break;
                }
                potValues.set(ctr, potValues.get(ctr)+(potMaxBet-prevPotMaxBet));
                prevPotMaxBet=potMaxBet;
            }
        }
    }
    
    List<PokerPlayer> getPlayersInPot(int potMaxBet){
        int inGamePlayerCount=this.activePlayerCount();
        List<PokerPlayer> playersInPot=new ArrayList(inGamePlayerCount);
        Iterable<Player> playersInGame=this.getActivePlayers();
        if(inGamePlayerCount<2){
            for(Player player:playersInGame){
                playersInPot.add((PokerPlayer)player);
            }
            return playersInPot;
        }
        PokerPlayer pokerPlayer;
        for(Player player:playersInGame){
            pokerPlayer=(PokerPlayer)player;
            if(pokerPlayer.chipsInPot>=potMaxBet && pokerPlayer.lastAction!=PlayerAction.folded){
                playersInPot.add(pokerPlayer);
            }
        }
        return playersInPot;
    }
    
    @Override
    protected Player newPlayer(Session session,int seatNo,JsonObject data) {
        return new PokerPlayer(session,seatNo,data);
    }

    @Override
    protected boolean canSeatBeTaken(Session session, int seatNo,JsonObject data) {
        //check if chipsOnTable >= min chips for table if not send error and return false
        int chipsOnTable=JsonUtil.getAsInt(data, "chipsOnTable", 0);
        if(chipsOnTable<(smallBlind*20)){
            StringBuilder errorDesc=new StringBuilder(70);
            errorDesc.append("Chips too less to take seat.");
            errorDesc.append("Min Chips ").append(smallBlind*20);
            errorDesc.append(" Chips used").append(chipsOnTable);
            this.setErrorDescription(errorDesc.toString());
            return false;
        }
        //check if chipsin hand >= chipsOnTable if not send error and return false
        PokerUserData userData=(PokerUserData)session.getGameUserData();
        if(userData.chipsInHand<chipsOnTable){
            StringBuilder errorDesc=new StringBuilder(70);
            errorDesc.append("Not enough chips in Hand.");
            errorDesc.append("Chips in Hand ").append(userData.chipsInHand);
            errorDesc.append(" Chips required").append(chipsOnTable);
            this.setErrorDescription(errorDesc.toString());
            return false;            
        }
        
        return true;
    }

    @Override
    protected JsonObject getRoomInfo() {
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("smallBlind", this.smallBlind);
        json.add("minChipsToTakeSeat", this.smallBlind*20);
        return json.build();
    }

    @Override
    protected JsonObject prepareJsonForClient() {
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        json.add("smallBlind", this.smallBlind);
        json.add("minChipsToTakeSeat", this.smallBlind*20);
        PokerPlayer curPlayer=(PokerPlayer)getCurTurnPlayer();
        int callValue=0;
        if(this.isGamePlayInProgress() && curPlayer!=null)
            callValue=maxBetInPot-curPlayer.chipsInPot;
        json.add("callValue", callValue);
        JsonArrayBuilder jsonPots=JsonUtil.createArrayBuilder();
        int noOfPots=potValues.size();
        int totPotAmt=0;
        int potValue;
        for(int ctr=0;ctr<noOfPots;ctr++){
            potValue=potValues.get(ctr);
            jsonPots.add(potValue);
            totPotAmt+=potValue;
        }
        json.add("pots", jsonPots);
        json.add("totPotAmt", totPotAmt);
        JsonArrayBuilder jsonTableCards=JsonUtil.createArrayBuilder();
        for(Card card:tableCards)
            jsonTableCards.add(card.toString());
        for(int ctr=tableCards.size();ctr<5;ctr++)
            jsonTableCards.add("");
        json.add("tableCards", jsonTableCards);
        json.add("dealerSeatNo", dealerSeatNo);
        json.add("smallBlindSeatNo", smallBlindSeatNo);
        json.add("bigBlindSeatNo", bigBlindSeatNo);
        return json.build();
    }

    @Override
    protected void onSeatTaken(Player player) {
        debug(player.session, "on seat taken");
        PokerUserData userData=(PokerUserData)player.session.getGameUserData();
        PokerPlayer pokerPlayer=(PokerPlayer)player;
        userData.chipsInHand-=pokerPlayer.chipsOnTable;
    }

    @Override
    protected void onRoomJoined(Session session){
        debug(session,"on room joined");
        User user=session.getUser();
        if(user.isBot){
            PokerUserData userData=(PokerUserData)session.getGameUserData();
            if(userData.chipsInHand<this.smallBlind*80)
                userData.chipsInHand=this.smallBlind*80;
        }
    }

    @Override
    protected void beforeGamePlayStarted() {
        tableCards.clear();
        bettingRound=PokerRound.preFlop;
        //set new dealerPosition;
        dealerSeatNo=this.getNextActivePlayer(dealerSeatNo).seatNo;
        PokerPlayer smallBlindPlayer=(PokerPlayer)this.getNextActivePlayer(dealerSeatNo);
        smallBlindSeatNo=smallBlindPlayer.seatNo;
        smallBlindPlayer.lastAction=PlayerAction.smallBlind;
        smallBlindPlayer.chipsInPot=smallBlind;
        smallBlindPlayer.chipsOnTable-=smallBlind;
        if(smallBlindPlayer.chipsOnTable==0)
            smallBlindPlayer.lastAction=PlayerAction.allIn;
        
        PokerPlayer bigBlindPlayer=(PokerPlayer)this.getNextActivePlayer(smallBlindSeatNo);
        bigBlindSeatNo=bigBlindPlayer.seatNo;
        bigBlindPlayer.lastAction=PlayerAction.bigBlind;
        bigBlindPlayer.chipsInPot=bigBlind;
        bigBlindPlayer.chipsOnTable-=bigBlind;
        if(bigBlindPlayer.chipsOnTable==0)
            bigBlindPlayer.lastAction=PlayerAction.allIn;
        
        maxBetInPot=bigBlind;
        calculatePots();
        //deal hole cards
        deck.recreateShuffled();
        PokerPlayer pokerPlayer=null;
        Iterable<Player> activePlayers=this.getActivePlayers();
        for(Player player:activePlayers){
            pokerPlayer=(PokerPlayer)player;
            pokerPlayer.setHoleCards(deck.deal(), deck.deal());
            this.debug(player.session, "hole cards=" + Utils.toCSV(pokerPlayer.hand));
        }        
    }

    @Override
    protected Player afterGamePlayStarted() {
        return this.getNextActivePlayerWhoCanPlayTurn(bigBlindSeatNo);
    }

    @Override
    protected void onTurnTimedOut(Player turnPlayer) {
        debug(turnPlayer.session, "Turn timed out");
        int callValue=maxBetInPot-((PokerPlayer)turnPlayer).chipsInPot;
        if(callValue==0){//do check
            JsonObjectBuilder data=JsonUtil.createObjectBuilder();
            data.add("betAmt", 0);
            this.forceAction(turnPlayer, "bet", data.build());
        }
        else //fold            
            this.forceAction(turnPlayer, "fold", null);
    }

    @Override
    protected void onPlayerAction(Player player, String action, JsonObject data, boolean isOutOfTurn) {
        debug(player.session,"onPlayerAction: " + action + " - " + data);
        if(isOutOfTurn && !action.equals("fold")){
            return;
        }
        JsonObjectBuilder actionData=null;
        PokerPlayer sitNGoTurnPlayer=(PokerPlayer)player;
        if(action.equals("bet")){
            int betAmt=JsonUtil.getAsInt(data, "betAmt");
            try{
                addChipsToPot(sitNGoTurnPlayer, betAmt);
                sitNGoTurnPlayer.lastBettingRound=this.bettingRound;
                actionData=JsonUtil.createObjectBuilder();
                actionData.add("amtBet", sitNGoTurnPlayer.lastAmtBet);
                actionData.add("raisedBy", sitNGoTurnPlayer.raisedBy);
            }catch(InvalidBetException ex){
                JsonObjectBuilder errorData=JsonUtil.createObjectBuilder();
                errorData.add("minBet", ex.minBet);
                errorData.add("actualBet", ex.actualBet);
                this.invalidateAction("Bet value too less.", errorData.build());
                return;
            }
        }
        else if(action.equals("fold")){
            if(sitNGoTurnPlayer.lastAction==PlayerAction.allIn || sitNGoTurnPlayer.lastAction==PlayerAction.folded){                
                return;
            }
            sitNGoTurnPlayer.lastAction=PlayerAction.folded;
            sitNGoTurnPlayer.raisedBy=0;
            sitNGoTurnPlayer.lastAmtBet=0;
            sitNGoTurnPlayer.exitGamePlay();
        }
        else{
            return;
        }
        this.actionPlayed(sitNGoTurnPlayer.lastAction.toString(), actionData==null?null:actionData.build());
    }

    @Override
    protected Player afterTurnPlayed(Player player, String action, JsonObject actionData) {
        PokerPlayer nextPlayer=(PokerPlayer)getNextActivePlayerWhoCanPlayTurn();

        if(nextPlayer==null){
            //deal remaining table cards and end game
            while(tableCards.size()<5)
                tableCards.add(deck.deal());
            bettingRound=PokerRound.river;
            debug("game ended - no valid players");            
            this.endGamePlay();
            return null;
        }
        //check if round has completed
        if(isRoundCompleted()){
            if(bettingRound==PokerRound.river){
                debug("game ended - rounds completed");
                this.endGamePlay();
                return null;
            }
            startNextRound();
            nextPlayer=(PokerPlayer)getNextActivePlayerWhoCanPlayTurn(dealerSeatNo);
            if(nextPlayer==null){
                //deal remaining table cards and end game
                while(tableCards.size()<5)
                    tableCards.add(deck.deal());
                bettingRound=PokerRound.river;
                debug("game ended - no valid players");            
                this.endGamePlay();
                return null;
            }
        }
        return nextPlayer;
    }

    @Override
    protected JsonObject getNextTurnData(Player nextPlayer) {
        JsonObjectBuilder turnData=JsonUtil.createObjectBuilder();
        turnData.add("callValue", maxBetInPot-((PokerPlayer)nextPlayer).chipsInPot);
        return turnData.build();
    }
    
    int playersNotFoldedCount(){
        Iterable<Player> activePlayers=this.getActivePlayers();
        int count=0;
        for(Player player:activePlayers){
            PokerPlayer pokerPlayer=(PokerPlayer)player;
            if(!(pokerPlayer.lastAction==PlayerAction.folded))
                count++;
        }
        return count;
    }
        
    void startNextRound(){
        JsonObjectBuilder json=JsonUtil.createObjectBuilder();
        JsonArrayBuilder jsonCards=JsonUtil.createArrayBuilder();
        Card card;
        switch(bettingRound){
            case preFlop:
                //do flop
                card=deck.deal();
                tableCards.add(card);
                jsonCards.add(card.toString());
                card=deck.deal();
                tableCards.add(card);
                jsonCards.add(card.toString());
                card=deck.deal();
                tableCards.add(card);
                jsonCards.add(card.toString());
                bettingRound=PokerRound.flop;
                debug("flop dealt " + tableCards);
                break;
            case flop:
                //do turn
                card=deck.deal();
                tableCards.add(card);
                jsonCards.add(card.toString());
                bettingRound=PokerRound.turn;
                debug("turn dealt " + tableCards);
                break;
            case turn:
                //do river
                card=deck.deal();
                tableCards.add(card);
                jsonCards.add(card.toString());
                bettingRound=PokerRound.river;
                debug("river dealt " + tableCards);
                break;
        }
        json.add("cards", jsonCards);
        this.gameAction(bettingRound.toString(), json.build());
        //clear lastAction of all Players
        Iterable<Player> activePlayers=this.getActivePlayers();
        PokerPlayer pokerPlayer;
        for(Player player:activePlayers){
            pokerPlayer=(PokerPlayer)player;
            if(pokerPlayer.lastAction!=PlayerAction.allIn)
                pokerPlayer.lastAction=PlayerAction.none;
        }
    }
    
    boolean isRoundCompleted(){
        //if all playersInGame are either allin or folded or (their chipsInPot>maxBetInPot and their lastBettingRound==bettinground) then round completed
        PokerPlayer pokerPlayer;
        Iterable<Player> activePlayers=getActivePlayers();
        for(Player player:activePlayers){
            pokerPlayer=(PokerPlayer)player;                   
            if((pokerPlayer.lastAction!=PlayerAction.allIn && pokerPlayer.lastAction!=PlayerAction.folded) && (pokerPlayer.chipsInPot<maxBetInPot || pokerPlayer.lastBettingRound!=this.bettingRound))
                return false;
        }
        debug(this.bettingRound + " round completed");
        return true;
    }

    @Override
    protected JsonObject beforeGamePlayEnded() {
        this.debug("before game ended, gameNo=" + this.getCurrentGamePlayNo());
        calculatePots();
        //get winners with winning cards and pot values for each pot-*
        int noOfPots=potMaxBets.size();
        long tableCardsLongVal=0;
        for(Card card:tableCards)
            tableCardsLongVal|=card.code;
        List<Winner> potWinners;
        JsonArrayBuilder jsonPotWinners;
        JsonArrayBuilder jsonPots=JsonUtil.createArrayBuilder();
        JsonObjectBuilder jsonPot;
        PokerPlayer winningPlayer=null;
        for(int ctr=0;ctr<noOfPots;ctr++){
            potWinners=getPotWinners(ctr, tableCardsLongVal);
            jsonPot=JsonUtil.createObjectBuilder();
            jsonPotWinners=JsonUtil.createArrayBuilder();
            for(Winner winner:potWinners){
                if(this.debugEnabled && winningPlayer==null)
                    winningPlayer=winner.player;
                jsonPotWinners.add(winner.toJson());
            }
            jsonPot.add("potValue", potValues.get(ctr));
            jsonPot.add("winners", jsonPotWinners);
            jsonPots.add(jsonPot);
        }
        if(this.debugEnabled){
            StringBuilder sb=new StringBuilder(128);
            sb.append("Winning data ");
            //print table cards
            sb.append("tableCards=").append(Utils.toCSV(this.tableCards));
            //print winning player cards and winning handCategory and winning player name
            if(winningHandDescription!=null){
                sb.append("; playerCards=").append(Utils.toCSV(winningPlayer.hand));
                sb.append("; handCategory=").append(winningPlayer.winningHandCategory);
                sb.append("; winner=").append(winningPlayer.session.getUser().getName());
            }
            this.debug(sb.toString());
        }
        JsonObjectBuilder gameEndData=JsonUtil.createObjectBuilder();
        gameEndData.add("pots", jsonPots);
        return gameEndData.build();
    }
    
    List<Winner> getPotWinners(int potIndex,long tableCardsLongVal){        
        List<Winner> winners=new ArrayList<>(2);
        List<PokerPlayer> playersInPot=null;        
        playersInPot=getPlayersInPot(potMaxBets.get(potIndex));        
        Winner winner=null;
        if(playersInPot.size()<2){
            winner=new Winner(playersInPot.get(0));
            winner.amtWonInPot=potValues.get(potIndex);
            winner.player.amtWonInGame+=winner.amtWonInPot;
            winners.add(winner);
            return winners;
        }
        //calculate 
        int maxEval=0;
        for(PokerPlayer player:playersInPot){
            if(player.handEVal==0){
                player.handEVal=HandEval.hand7Eval(tableCardsLongVal|player.hand.get(0).code | player.hand.get(1).code);
            }
            if(maxEval<player.handEVal)
                maxEval=player.handEVal;
        }
        //find all players whose handEval==maxEval and add to winners
        for(PokerPlayer player:playersInPot){
            if(player.handEVal==maxEval)
                winners.add(new Winner(player));
        }
        int playerAmtWon=potValues.get(potIndex)/winners.size();
        for(Winner winner2:winners){
            winner2.amtWonInPot=playerAmtWon;
            winner2.player.amtWonInGame+=playerAmtWon;
            if(winner2.player.winningCards==null){
                winner2.player.winningHandCategory=HandEval.getHandCategory(maxEval);
                winner2.player.winningCards=HandEval.getWinningCards(maxEval, winner2.player.hand, tableCards);
            }
        }
        return winners;
    }

    @Override
    protected void afterGamePlayEnded() {
        PokerPlayer pokerPlayer;
        for(Player player:this.getPlayersWhoPlayed()){
            pokerPlayer=(PokerPlayer)player;
            pokerPlayer.chipsOnTable+=pokerPlayer.amtWonInGame;
        }
    }

    @Override
    protected void resetData() {
        tableCards.clear();
        potMaxBets.clear();
        potValues.clear();
        maxBetInPot=0;       
    }

    @Override
    protected void beforeSeatLeft(Player player, boolean isCurTurnPlayer, boolean isLeavingRoom, boolean isLoggingOut) {
        debug(player.session,"on seat left");
        PokerUserData userData=(PokerUserData)player.session.getGameUserData();
        PokerPlayer pokerPlayer=(PokerPlayer)player;
        userData.chipsInHand+=pokerPlayer.chipsOnTable;
        if(player.isActive())
            this.forceAction(player, "fold", null);
    }

    @Override
    protected void onRoomLeft(Session session, boolean isLoggingOut) {
        debug(session, "on room left");
    }

    @Override
    protected JsonObject getAdditionalRoomConfig() { //for private rooms additional config
        return null;
    }

    @Override
    protected void setAdditionalRoomConfig(JsonObject additionalRoomConfig) {
    }

}
