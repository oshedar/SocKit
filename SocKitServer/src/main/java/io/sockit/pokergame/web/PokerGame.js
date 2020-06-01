        console.log("pokergame version 1.0");
        var pokerGame=new PokerGame("wss://" + document.location.hostname);
        var gameName="SitNGo";
        var noOfSeats=5;
        var middleSeatNo=4;
        var clubSrc="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxODYuNzkgMTk5LjUiPjx0aXRsZT5jbHViPC90aXRsZT48ZyBpZD0iTGF5ZXJfMiIgZGF0YS1uYW1lPSJMYXllciAyIj48ZyBpZD0iTGF5ZXJfMS0yIiBkYXRhLW5hbWU9IkxheWVyIDEiPjxwYXRoIGlkPSJwYXRoMTQ3LTAtOC0xLTYtMS0xLTAtOS0xLTgiIGQ9Ik05My4zOCwwYy00MC43NS44OS01OSw0Ni43Ny0yOC44NSw4Mi40MSw0LjM4LDUuMTksNC44Myw4LjE4LTMuMTgsMy4xOEM0MSw3MiwyLjA2LDgwLjk0LjA2LDEyMi40OC0yLjQsMTczLjY3LDY5LjEyLDE4OC40Miw4Mi41NSwxMzRjLjcxLTUuNzQsNS4zOC01LjYxLDQuMjUsMi41NC0xLjIyLDIyLjEyLTguMTMsNDIuOC0xNy42Myw2M2g0OC40NWMtOS40OS0yMC4xNy0xNi40LTQwLjg1LTE3LjYzLTYzLTEuMTItOC4xNSwzLjU0LTguMjgsNC4yNS0yLjU0LDEzLjQzLDU0LjQzLDg1LDM5LjY4LDgyLjQ5LTExLjUxLTItNDEuNTQtNDEtNTAuNDgtNjEuMjgtMzYuODktOCw1LTcuNTcsMi0zLjE5LTMuMThDMTUyLjQ0LDQ2Ljc3LDEzNC4xNy44OSw5My40MSwwaDBaIi8+PC9nPjwvZz48L3N2Zz4=";
        var diamondSrc="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNDEuNTcgMjA2Ljc4Ij48ZGVmcz48c3R5bGU+LmNscy0xe2ZpbGw6I2Q0MDAwMDt9PC9zdHlsZT48L2RlZnM+PHRpdGxlPmRpYW1vbmQ8L3RpdGxlPjxnIGlkPSJMYXllcl8yIiBkYXRhLW5hbWU9IkxheWVyIDIiPjxnIGlkPSJMYXllcl8xLTIiIGRhdGEtbmFtZT0iTGF5ZXIgMSI+PHBhdGggaWQ9InBhdGg2NjAwLTUiIGNsYXNzPSJjbHMtMSIgZD0iTTcwLjc5LDIwNi43OEM0OS43NiwxNjkuODksMjcuNjQsMTM0LDAsMTAzLjM5LDI3LjY0LDcyLjc2LDQ5Ljc2LDM2LjksNzAuNzksMGMyMSwzNi45LDQzLjE0LDcyLjc2LDcwLjc4LDEwMy4zOUMxMTMuOTMsMTM0LDkxLjgxLDE2OS44OSw3MC43OSwyMDYuNzhaIi8+PC9nPjwvZz48L3N2Zz4=";
        var heartSrc="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNDkuNiAxODYuNDYiPjxkZWZzPjxzdHlsZT4uY2xzLTF7ZmlsbDojZDQwMDAwO308L3N0eWxlPjwvZGVmcz48dGl0bGU+aGVhcnQ8L3RpdGxlPjxnIGlkPSJMYXllcl8yIiBkYXRhLW5hbWU9IkxheWVyIDIiPjxnIGlkPSJMYXllcl8xLTIiIGRhdGEtbmFtZT0iTGF5ZXIgMSI+PHBhdGggaWQ9InBhdGg5Njk4LTAtMCIgY2xhc3M9ImNscy0xIiBkPSJNNzQuOCwxODYuNDZzLTE4LjQ5LTI5Ljc0LTQzLjQzLTY0LjdDMTQuNjgsOTguMzYsMS4zNCw3MywuMSw1MS4yLTEuMzIsMjYuMjcsMTIuMTUsMS4yNiwzNi41Mi4wNVM3MC43OCwxOS4yNyw3NC44LDM2LjEyYzQtMTYuODUsMTMuOTItMzcuMjgsMzguMjgtMzYuMDdTMTUwLjkyLDI2LjI3LDE0OS41LDUxLjJjLTEuMjQsMjEuODItMTQuNTgsNDcuMTYtMzEuMjcsNzAuNTZDOTMuMjksMTU2LjcyLDc0LjgsMTg2LjQ2LDc0LjgsMTg2LjQ2WiIvPjwvZz48L2c+PC9zdmc+";
        var spadeSrc="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNTggMTk3LjEiPjx0aXRsZT5zcGFkZTwvdGl0bGU+PGcgaWQ9IkxheWVyXzIiIGRhdGEtbmFtZT0iTGF5ZXIgMiI+PGcgaWQ9IkxheWVyXzEtMiIgZGF0YS1uYW1lPSJMYXllciAxIj48cGF0aCBpZD0icGF0aDY4MjgtOC0zIiBkPSJNNzksMEM0MS4xNiw1NCwuNjgsODUuMTIsMCwxMjcuMjdjLS4yMywxMy42NSw3LjE0LDM2Ljg1LDMwLjIyLDQxLjI5LDE1LDIuODYsMzUuNi05LjI3LDM2LTMzLjkzLS4wNi00LjkxLDUuMi00Ljc4LDUuMTUsMS45NC0uNzEsMTkuNDUtNi44Niw0Mi4xLTE3LjE5LDYwLjUzaDQ5LjU2QzkzLjQ1LDE3OC42Nyw4Ny4zLDE1Niw4Ni41OSwxMzYuNTdjMC02LjcyLDUuMjEtNi44NSw1LjE1LTEuOTQuNDMsMjQuNjYsMjEuMDUsMzYuNzksMzYsMzMuOTMsMjMuMDgtNC40NCwzMC40NS0yNy42NCwzMC4yMi00MS4yOUMxNTcuMzIsODUuMTIsMTE2Ljg0LDU0LDc5LDBaIi8+PC9nPjwvZz48L3N2Zz4=";
        
        function init(){
            gapi.load('auth2', function() {
                var config={
                    client_id: 'MYID.apps.googleusercontent.com',
                    cookiepolicy: 'single_host_origin',
                    ux_mode: 'redirect',
                    redirect_uri: ''
                    // Request scopes in addition to 'profile' and 'email'
                    //scope: 'additional_scope'
                };
                config.redirect_uri="http://" + document.location.hostname + "/index.html";
                gapi.auth2.init(config);
            });
        }

        function onSignIn(googleUser) {
            console.log("signed into google");
            pokerGame.loginWithGoogle(googleUser.getAuthResponse().id_token.toString());
        }
           
        function logout(){
            pokerGame.client.logOut();
        }
        
        function PokerGame(url){
            this.client=null;
            this.url=url;
            this.lastLocationQueried=null;
            this.isLoggedIn=function (){
                return this.client!=null && this.client.isLoggedIn();
            };
            
            this.rejoinSession=function(){
                if(this.client==null){
                    this.client=new Client(this.url);
                    this.client.setClientEventListener(this);
                }
                return this.client.rejoinSession();
            };
            
            this.registerUser=function(emailId,password,name){
                if(this.client==null){
                    this.client=new Client(this.url);
                    this.client.setClientEventListener(this);
                }
                if(!this.client.isLoggedIn())
                    this.client.registerWithEmailId(emailId,password,name,gameName);                
            };
            
            this.loginWithOtherId=function(otherId,name){
                if(this.client==null){
                    this.client=new Client(this.url);
                    this.client.setClientEventListener(this);
                }
                if(!this.client.isLoggedIn())
                    this.client.logInWithOtherId(otherId,name,gameName);
            };
            
            this.loginWithEmailId=function(emailId,password){
                if(this.client==null){
                    this.client=new Client(this.url);
                    this.client.setClientEventListener(this);
                }
                if(!this.client.isLoggedIn())
                    this.client.logInWithEmailId(emailId,password,gameName);
            };
            
            this.loginWithGoogle=function(idToken){
                if(this.client==null){
                    this.client=new Client(this.url);
                    this.client.setClientEventListener(this);
                }
                if(!this.client.isLoggedIn())
                    this.client.logInWithGoogle(idToken,gameName);
            };
            
            this.onError=function(client, errorCode, errorDesc){
                console.log("serverError: " + errorDesc);
            };
            
            this.onLoggedIn=function(client,isGameSelected) {
                sessionStorage.setItem("pokergame_emailId",client.getEmailId());                
                sessionStorage.setItem("pokergame_userName",client.getName());                
                if(client.hasJoinedRoom()){
                    this.onRoomJoined(client,client.getJoinedRoom());
                    return;
                }
                if(isGameSelected)
                    getLocations();
            };
            
            this.onLoggedOut=function(client) {
                if(isInLobbyScreen()){
                    _animateLobbyOut("login", function(){
                        _animateLoginIn("lobby");
                        });
                }
                else if(isInGameScreen()){
                    _animateGameOut(function(){
                        _animateLoginIn("game");
                        });                    
                }
                else{
                    _animateLoginIn();
                }
            };
            
            this.onSessionTimedOut=function(client) {
                this.onLoggedOut(client);
            };
            
            this.onGetLocations=function(client, gameName, locations) {
                lobbyType="locations";
                if(isInLobbyScreen()){
//                    reloadLobbyTable(fillLocationList.bind(null,client, gameName, locations));
                    fillLocationList(client, gameName, locations);
                    return;
                }
                if(isInGameScreen()){
                    fillLocationList(client, gameName, locations);
                    _animateGameOut(_animateLobbyIn.bind(null,"game"));
                    return;                    
                }
                //fullScreen();
                fillLocationList(client, gameName, locations);
                _animateLoginOut(_animateLobbyIn.bind(null,"login"));
                return;
            };
                       
            this.onGetRooms=function(client, gameName, location, roomtype, rooms) {
                lobbyType="rooms";
                if(isInLobbyScreen()){
//                    reloadLobbyTable(fillRoomList.bind(null,client, gameName, location, roomtype, rooms));
                    fillRoomList(client, gameName, location, roomtype, rooms);
                    return;
                }
                if(isInGameScreen()){
                    fillRoomList(client, gameName, location, roomtype, rooms);
                    _animateGameOut(_animateLobbyIn.bind(null,"game"));
                    return;                    
                }
                fillRoomList(client, gameName, location, roomtype, rooms);
                _animateLoginOut(_animateLobbyIn.bind(null,"login"));
                return;
            };
            
            this.onRoomJoined=function(client, room) {
                this.lastLocationQueried=room.location;
                hideAllPlayerPopOvers();
                if(1===1){
                    disableRenderRoom();
                    _animateLobbyOut("game",function(){
//                        document.getElementById("players").classList.add("hidden");
                        _animateGameIn();
                    });
                }
                else{
                    disableRenderRoom();
                    _animateLoginOut(function(){                        
//                        document.getElementById("players").classList.add("hidden");
                        _animateGameIn();
                    });                    
                }
            };
            
            this.onRoomRefreshedFromServer=function(client, room) {
                renderRoom();
            };
            
            this.onSeatTaken=function(client, playerSeated, room,isSelf) {
                var rotateBy=getRotateBy();
                var delay=0;
                //hide popover
                getSeatElement(playerSeated.seatNo).querySelector(".popover").classList.add("hidden");
                if(isSelf && rotateBy!=0){
                    disableRenderRoom();
                    var startFrom=playerSeated.seatNo-1;                    
                    //snimateSeatsOut                    
                    for(var ctr=startFrom;true;){
                        if(ctr+1===startFrom || (startFrom==0 && ctr==seatElements.length-1)){
                            setTimeout(animateSeatOut.bind(null,seatElements[ctr],function(){
                                    renderRoom(true);
                                    var delay2=0;
                                    var startFrom2=middleSeatNo-1;                                    
                                    for(var ctr2=startFrom2;true;){
                                        setTimeout(_animateSeatIn.bind(null,seatElements[ctr2],ctr2+1===startFrom),delay2);
                                        delay2+=100;
                                        ctr2++;
                                        if(ctr2===seatElements.length)
                                            ctr2=0;
                                        if(ctr2===startFrom2)
                                            break;
                                    }
                                }),delay);
                        }
                        else{
                            setTimeout(animateSeatOut.bind(null,seatElements[ctr]),delay);
                        }
                        delay+=100;
                        ctr++;
                        if(ctr===seatElements.length)
                            ctr=0;
                        if(ctr===startFrom)
                            break;
                    }
                }
                else{
                    disableRenderRoom();
                    animateSeatOut(getSeatElement(playerSeated.seatNo),renderRoomAndAnimateSeatIn.bind(null,getSeatElement(playerSeated.seatNo)));
                }
                
            };
            
            this.beforeServerMessageProcessed=function(client,serverMessageType,customCommand,data){
                
            };
            
            this.afterServerMessageProcessed=function(client,serverMessageType,customCommand,isBinary,data){
                setTimeout(setTopButtonHoverLabel.bind(null),100);
                renderClientInfo();
            };
            
            this.onServerShutdown=function(client) {
                console.log("server shutdown");
                animateConnectionError("Server Shutdown","start",false);
                this.onLoggedOut();
            };
            
            this.onRoomLeft=function(client) {
                disableTopButton();
                hideAllPlayerPopOvers();
                if(pokerGame.lastLocationQueried)
                    getRooms(pokerGame.lastLocationQueried);
                else
                    getLocations();                
            };
            
            this.onInvalidAction=function(client,action,description,errorData,room,isOutOfTurn) {
                console.log("Invalid Action : " + description);
            };
            
            this.onSeatLeft=function(client, playerLeft, room, isSelf) {
                disableRenderRoom();
                disableTopButton();
                var seatElement;
                if(isSelf){
                    seatElement=getSeatElement2(playerLeft.seatNo,playerLeft.seatNo);
                }
                else
                    seatElement=getSeatElement(playerLeft.seatNo);
                if(isSelf){
                    disableRenderRoom();
                    if(areWinnersAnimating())
                        _animateWinnersStop(animateSeatOut.bind(null,seatElement,renderRoomAndAnimateSeatIn.bind(null,seatElement)));
                    else
                        animateSeatOut(seatElement,renderRoomAndAnimateSeatIn.bind(null,seatElement));
                }
                else if(seatElement.classList.contains("winner")){
                    disableRenderRoom();
                    if(areWinnersAnimating())
                        _animateWinnersStop(animateSeatOut.bind(null,seatElement,renderRoomAndAnimateSeatIn.bind(null,seatElement)),seatElement);
                    else
                        animateSeatOut(seatElement,renderRoomAndAnimateSeatIn.bind(null,seatElement))
                }
                else{
                    disableRenderRoom();
                    animateSeatOut(seatElement,renderRoomAndAnimateSeatIn.bind(null,seatElement));
                }
            };
            
            this.onMessageReceivedBytes=function(client,command, data) {
            };
            
            this.onMessageReceivedJson=function(client,command, data) {
            };
            
            this.onMessageReceivedString=function(client,command, data) {
            };
            
            this.onErrorMessageReceivedJson=function(client, errorCode, errorData) {
                console.log("error " + JSON.stringify(errorData));
            };
            
            this.onErrorMessageReceivedString=function(client, errorCode, errorDesc) {

            };    
            
            this.onGamePlayStarted=function(client, room) {
                System.debug("\ngame started gameNo=" + room.getGamePlayNo());
                renderRoom();
            };
            
            this.onNextTurn=function(client, turnPlayer, turnData, room, isSelfTurn){ 
                var curTime=Date.now();
                if(curTime-disableRenderTime<2500){
                    var delay=2500-(curTime-disableRenderTime);                    
                    setTimeout(renderRoom.bind(null),delay<1?1:delay);
                }
                else
                    renderRoom();                
            };
            
            this.onTurnPlayed=function(client, turnPlayer, playerAction, actionData, room, isSelf) {        
                renderSeat(room,turnPlayer,getSeatElement(turnPlayer.seatNo),turnPlayer.seatNo,false);
                renderPot(room.getData());
                stopTimerAnimation(getSeatElement(turnPlayer.seatNo));                
            };
            
            this.onOutOfTurnPlayed=function(client, outOfTurnPlayer, playerAction, actionData, room, isSelf) {
                renderSeat(room,outOfTurnPlayer,getSeatElement(outOfTurnPlayer.seatNo),outOfTurnPlayer.seatNo);
                if(isSelf)
                    showActionButtons(client,false);
            };
            
            this.onGameAction=function(client, gameAction, actionData, room) {
                renderRoom();
            };
            
            this.onGamePlayEnded=function(client, room, endGameData) {
                System.debug("game ended, gameNo=" + room.getGamePlayNo());
                renderRoom(false,true);
                //render winning data
                var winningHandCategory;
                var winningPlayer;
                var winningCards,winningCategoryCards;
                var winningSeatNo;
                var winningSeatElement;
                var ctr;
                var seatElement,playerGameData;
                for(ctr=0;ctr<endGameData.pots[0].winners.length;ctr++){
                    //highlight winner
                    winningSeatNo=endGameData.pots[0].winners[ctr].seatNo;
                    winningPlayer=room.getPlayerBySeatNo(winningSeatNo);
                    winningSeatElement=getSeatElement(winningSeatNo);
                    winningSeatElement.classList.add("winner");
                    winningHandCategory=endGameData.pots[0].winners[ctr].handCategory;
                    winningCards=endGameData.pots[0].winners[ctr].winningCards;
                    winningCategoryCards=this.getWinningCategoryCards(winningHandCategory,winningCards);
                    //highlight winning cards in hand
                    if(winningCards){
                        seatElement=getSeatElement(winningSeatNo);
                        playerGameData=winningPlayer.getData();
                        if(winningCategoryCards.includes(playerGameData.hand[0]))
                            seatElement.querySelector(".cards > div:first-child").classList.add("highlight");
                        if(winningCategoryCards.includes(playerGameData.hand[1]))
                            seatElement.querySelector(".cards > div:last-child").classList.add("highlight");                        
                    }
                }
                
                if(winningHandCategory){
                    if(winningHandCategory==="No Pair")
                        winningHandCategory="High Card";
                    document.querySelector("#pot > span").innerText=winningHandCategory;
                }
                //highlight winning cards on table
                if(winningCards){
                    var roomData=room.getData();
                    var cardsSection=document.getElementById("cards");
                    for(ctr=0;ctr<roomData.tableCards.length;ctr++){
                        if(winningCategoryCards.includes(roomData.tableCards[ctr]))
                            cardsSection.querySelector("li:nth-child("+(ctr+1) + ")").classList.add("highlight");                        
                    }
                }
                //set amt won
                var players=room.getPlayers();
                for(ctr=0;ctr<players.length;ctr++){
                    playerGameData=players[ctr].getData();                        
                    if(playerGameData.amtWon>0){                            
                        getSeatElement(players[ctr].seatNo).querySelector(".cashOnTable").innerText="Won $" + playerGameData.amtWon;
                    }
                }
                //start winnerAnimation
                animateWinners("start");
            };
            
            this.getWinningCategoryCards=function(handCategory,winningCards){
                var cards=[];
                if(handCategory==null || winningCards==null)
                    return cards;
                switch(handCategory){
                    case "No Pair":
                        cards.push(winningCards[0]);
                        break;
                    case "Pair":
                        cards.push(winningCards[0]);
                        cards.push(winningCards[1]);
                        break;
                    case "Two Pair":
                        cards.push(winningCards[0]);
                        cards.push(winningCards[1]);
                        cards.push(winningCards[2]);
                        cards.push(winningCards[3]);
                        break;
                    case "Three Of A Kind":
                        cards.push(winningCards[0]);
                        cards.push(winningCards[1]);
                        cards.push(winningCards[2]);
                        break;
                    case "Four Of A Kind":
                        cards.push(winningCards[0]);
                        cards.push(winningCards[1]);
                        cards.push(winningCards[2]);
                        cards.push(winningCards[3]);
                        break;
                    default :
                        cards.push(winningCards[0]);
                        cards.push(winningCards[1]);
                        cards.push(winningCards[2]);
                        cards.push(winningCards[3]);
                        cards.push(winningCards[4]);
                }
                return cards;
            };
                        
            this.onRoomDestroyed=function(client, room) {
            };
            
            this.onConnectionSuccess=function(client) {
            };
            
            this.onConnectionFailure=function(client) {
            };

            this.onAvtarChangedOfSelf=function(client, newAvatarId) {
            };
            
            this.onAvtarChangedOfOther=function(client, player, newAvatarId, room) {
            };

            this.onNotEligibleToPlay=function(client, player, room, reason) {
                console.log("not elligible to play : " + reason);
            };
            
            this.onSessionRejoined=function(client){
                System.debug("session rejoined");
                if(client.hasJoinedRoom()){
                    this.lastLocationQueried=client.getJoinedRoom().location;
                    disableRenderRoom();
                    _animateGameIn(null,true);
                    return;
                }
                getLocations();
            };
            
            this.selectSeat=function(seatNo,chipsToTakeSeat){
                //check if client is already seated
                if(this.client.isSeated())
                    return;
                var takeSeatData=new Object();
                takeSeatData.chipsOnTable=chipsToTakeSeat;
                this.client.takeSeat(this.client.getJoinedRoom().roomId,seatNo,takeSeatData);                
            };
            
            this.onConnecting=function(client){
//                animateConnectionError("Connecting","start",true);
            };
            
            this.onConnectionSuccess=function(client){
                animateConnectionError("_","stop",false);
            };
            
            this.onConnectionFailure=function(client){
                animateConnectionError("Unable to Connect to Server","start",false);
            };
            
            this.onConnectionDisconnected=function(client){
              animateConnectionError("Connection to Server Broken","start",false);
            };
            
            this.onConnectionClosed=function(client){
                if(!this.isLoggedIn()){
//                    animateConnectionError("Connection Closed","stop",false);  
                    return;
                }
                animateConnectionError("Connection Closed","start",false);
            };
                        
        }
        
        var getLocationsTime=0;
        function getLocations(){
            if(Date.now()-getLocationsTime<1000){
                return;
            }
            getLocationsTime=Date.now();
            if(!pokerGame.isLoggedIn())
                return;
            pokerGame.client.getLocations();
        }
        
        var getRoomsTime=0;
        function getRooms(location){
            if(Date.now()-getRoomsTime<1000){
                return;
            }
            getRoomsTime=Date.now();
            pokerGame.client.getRooms(location,"normal");
        }
        
        function removeWinnerFromSeats(){
            for(var ctr=0;ctr<seatElements.length;ctr++){
                seatElements[ctr].classList.remove("winner");
            }
        }
        
        function removeWinnerFromSeat(seatElement){
            seatElement.classList.remove("winner");
        }
        
        function onCallClick(){            
            if(!pokerGame.isLoggedIn()){
                hideActionButtons();
                return;
            }
            if(!pokerGame.client.isGamePlayInProgress()){
                hideActionButtons();
                return;
            }
            var room=pokerGame.client.getJoinedRoom();
            var player=pokerGame.client.getPlayer();
            document.querySelector("#actions .popover").classList.add("hidden");
            if(player){
                if(!player.isCurTurn()){
                    disableCallRaiseButtons();
                    return;
                }
                var data=new Object();
                data.betAmt=room.getData().callValue;
                if(data.betAmt>player.getData().chipsOnTable)
                    data.betAmt=player.getData().chipsOnTable;
                pokerGame.client.playAction("bet", data);
                disableAllActionButtons();
            }
        }
        
        function onFoldClick(){
            hideActionButtons();
            if(!pokerGame.isLoggedIn()){
                return;
            }
            if(!pokerGame.client.isGamePlayInProgress()){
                return;
            }
            var room=pokerGame.client.getJoinedRoom();
            var player=pokerGame.client.getPlayer();
            document.querySelector("#actions .popover").classList.add("hidden");
            if(player){
                if(!player.isActive()){
                    return;
                }
                pokerGame.client.playAction("fold", null);
            }
        }
        
        function onRaiseClick(){
            if(!pokerGame.isLoggedIn()){
                hideActionButtons();
                return;
            }
            if(!pokerGame.client.isGamePlayInProgress()){
                hideActionButtons();
                return;
            }
            var popover=document.querySelector("#actions .popover");            
            var room=pokerGame.client.getJoinedRoom();
            var player=pokerGame.client.getPlayer();            
            if(player){
                if(!player.isCurTurn()){
                    disableCallRaiseButtons();
                    return;
                }
                var minRaiseBet=getMinRaiseBet(room.getData());
                var chipsOnTable=pokerGame.client.getPlayer().getData().chipsOnTable;
                if(chipsOnTable<=minRaiseBet){
                    var data=new Object();
                    popover.classList.add("hidden");
                    data.betAmt=chipsOnTable;
                    pokerGame.client.playAction("bet", data);
                    disableAllActionButtons();
                    return;
                }
                if(popover.classList.contains("hidden")){
                    //show oppover
                    var rangeInput=popover.querySelector("input[type=\"range\"]");
                    var textInput=popover.querySelector("span input");
                    rangeInput.setAttribute("min",minRaiseBet);
                    rangeInput.setAttribute("max",player.getData().chipsOnTable);
                    rangeInput.value=minRaiseBet;
                    textInput.setAttribute("min",minRaiseBet);
                    textInput.setAttribute("max",player.getData().chipsOnTable);
                    textInput.value=minRaiseBet;
                    popover.classList.remove("hidden");
//                    popover.querySelector("span input").focus();
                    document.querySelector("#actions .actionButton.raise").innerText="Bet";
                }
                else{
                    var amtBet=parseInt(popover.querySelector("span input").value);
                    var data=new Object();
                    popover.classList.add("hidden");
                    data.betAmt=amtBet;
                    pokerGame.client.playAction("bet", data);
                    disableAllActionButtons();
                }
            }
        }
        
        
        function getMinRaiseBet(roomData){
            var callValue=roomData.callValue;
            var bigBlind=roomData.smallBlind*2;
            var minRaiseBet=callValue*2;
            if(minRaiseBet<callValue+bigBlind)
                minRaiseBet=callValue+bigBlind;
            return minRaiseBet;
        }
        
        function onAllIn(){
            document.querySelector("#actions .popover").classList.add("hidden");
            if(!pokerGame.isLoggedIn())
                return;
            if(!pokerGame.client.isGamePlayInProgress())
                return;
            var chipsOnTable=pokerGame.client.getPlayer().getData().chipsOnTable;
            var data=new Object();
            data.betAmt=chipsOnTable;
            pokerGame.client.playAction("bet", data);
            return;
        }
        
        var seatElements=[];
        
        var disableRenderTime;
        function disableRenderRoom(){
            disableRenderTime=Date.now();            
        }

        function enableRenderRoom(){
            disableRenderTime=0;
        }
        
        function initSeatElements(){
            if(seatElements.length!==noOfSeats){
                seatElements=[];
                //remove extra seats
                var topRow=document.querySelector("#players > .top");
                var topRowSeats=document.querySelector("#players > .top").querySelectorAll(".player");
                var middleRowSeats=document.querySelector("#players > .middle").querySelectorAll(".player");
                var bottomRowSeats=document.querySelector("#players > .bottom").querySelectorAll(".player");
                topRowSeats[0].classList.remove("hidden");
                topRowSeats[1].classList.add("hidden");
                topRowSeats[2].classList.add("hidden");
                topRowSeats[3].classList.remove("hidden");
                middleRowSeats[0].classList.remove("hidden");
                middleRowSeats[1].classList.remove("hidden");
                bottomRowSeats[0].classList.add("hidden");
                bottomRowSeats[1].classList.remove("hidden");
                bottomRowSeats[2].classList.add("hidden");
                seatElements.push(topRowSeats[0]);
                seatElements.push(topRowSeats[3]);
                seatElements.push(middleRowSeats[1]);
//                seatElements.push(bottomRowSeats[2]);
                seatElements.push(bottomRowSeats[1]);
//                seatElements.push(bottomRowSeats[0]);
                seatElements.push(middleRowSeats[0]);
            }            
        }
        
        function renderRoom(forceRender,gameEnded){
            if(Date.now()-disableRenderTime<2500 && !forceRender){
                return;
            }
                        
            var ctr;
            if(pokerGame.isLoggedIn()===false || pokerGame.client.hasJoinedRoom()===false){
                console.error("client not logged in or  joined room");
                return;
            }
//            console.log("in render room");
            var client=pokerGame.client;
            var room=client.getJoinedRoom();

            initSeatElements();
            
            if(!gameEnded){
                _animateWinnersStop();                
            }
            
            for(ctr=1;ctr<=room.totalNoOfSeats;ctr++){
                renderSeat(room,room.getPlayerBySeatNo(ctr),getSeatElement(ctr),ctr,gameEnded);
            }
            var roomData=room.getData();
            
            renderTableCards(roomData);

            renderPot(roomData);
            
            showActionButtons(client,gameEnded);
        }
        
        function hideAllSeats(){
            for(var ctr=0;ctr<seatElements.length;ctr++){
                seatElements[ctr].querySelector(".popover").classList.add("hidden");
                seatElements[ctr].classList.add("hidden");
                seatElements[ctr].classList.remove("winner");
            }
        }
        
        function hideTableCards(){
            var cardElement;
            var cardsSection=document.getElementById("cards");
            for(var ctr=0;ctr<5;ctr++){
                cardElement=cardsSection.querySelector("li:nth-child("+(ctr+1) + ")");
                cardElement.classList.remove("highlight");
                cardElement.classList.add("placeholder");
            }            
        }
        
        function hidePots(){
            document.querySelector("#pot > span").innerText="";            
        }
        
        function renderPot(roomData){
            if(roomData.totPotAmt<1){
                document.querySelector("#pot > span").innerText="";
            }
            else{
                document.querySelector("#pot > span").innerText="$" + roomData.totPotAmt;
            }
            document.querySelector("#pot > span").classList.remove("hidden");
        }
        
        function renderTableCards(roomData){
            var rank,suite;
            var cardsSection=document.getElementById("cards");
            var cardElement;
            var ctr;
            for(ctr=0;ctr<roomData.tableCards.length;ctr++){
                cardElement=cardsSection.querySelector("li:nth-child("+(ctr+1) + ")");
                cardElement.classList.remove("highlight");
                if(roomData.tableCards[ctr]==""){
                    cardElement.classList.add("placeholder");
                }
                else{
                    rank=roomData.tableCards[ctr].substring(0,1);
                    suite=roomData.tableCards[ctr].substring(1,2);
                    if(rank=="T")
                        rank="10";
                    cardElement.querySelector(".numeral").textContent=rank;
                    cardElement.querySelector("img").setAttribute("src",getSuiteUrl(suite));
                    cardElement.querySelector("img").setAttribute("class",getSuiteClass(suite));
                    cardElement.classList.remove("placeholder");
                }                
            }            
        }
        
        function renderRoomAndAnimateSeatIn(seatElement){
            renderRoom(true);
            _animateSeatIn(seatElement,true);            
        }
        
        function renderSeat(room,player,seatElement,seatNo,gameEnded){
            seatElement.querySelector(".chip").setAttribute("class","chip hidden");
            if(player==null){ //seat is empty                
                seatElement.classList.add("empty");
                seatElement.classList.remove("you");
                seatElement.classList.remove("active");
                seatElement.classList.add("dim");
                stopTimerAnimation(seatElement);
                if(!pokerGame.client.isSeated()){
                    var minChips=room.getData().minChipsToTakeSeat;
                    var chipsInHand=pokerGame.client.gameUserData.chipsInHand;
                    if(chipsInHand>=minChips){
                        seatElement.querySelector(".emptySeat").innerText="Choose Seat";
                        seatElement.classList.add("fakeLink");
                        seatElement.classList.remove("dim");
                    }
                    else{
                        showError("Too few Chips to take seat");
                        seatElement.querySelector(".emptySeat").innerText="Too few Chips";
                        seatElement.classList.remove("fakeLink");
                    }
                }
                else{
                    seatElement.querySelector(".emptySeat").innerText=" ";
                    seatElement.classList.remove("fakeLink");
                }
                seatElement.classList.remove("hidden");
            }
            else{ // seat is not empty                
                var playerGameData=player.getData();
                seatElement.classList.remove("fakeLink");
                //render player
                if(player.isSameAsClient(pokerGame.client))
                    seatElement.classList.add("you");
                else 
                    seatElement.classList.remove("you");
                seatElement.classList.remove("empty");
                //set profile pic
                if(player.getProfilePic()==null)
                    seatElement.querySelector(".image > img").setAttribute("src","images/profile-picture.svg");
                else
                    seatElement.querySelector(".image > img").setAttribute("src",player.getProfilePic());
                //set cards
                if(playerGameData.hand==null || playerGameData.hand.length<1) //!player.isActive()
                    seatElement.querySelector(".cards").classList.add("hidden");
                else{
                    seatElement.querySelector(".cards").classList.remove("hidden");
                    seatElement.querySelector(".cards > div:first-child").classList.remove("highlight");
                    seatElement.querySelector(".cards > div:last-child").classList.remove("highlight");
                    if(playerGameData.hand[0]=="-")
                        seatElement.querySelector(".cards").classList.add("facingDown");
                    else {
                        var hand=playerGameData.hand;
                        var rank,suite;
                        seatElement.querySelector(".cards").classList.remove("facingDown");
                        rank=hand[0].substring(0,1);
                        suite=hand[0].substring(1,2);
                        if(rank=="T")
                            rank="10";
                        seatElement.querySelector(".cards > div:first-child .numeral").textContent=rank;
                        seatElement.querySelector(".cards > div:first-child img").setAttribute("src",getSuiteUrl(suite));
                        seatElement.querySelector(".cards > div:first-child img").setAttribute("class",getSuiteClass(suite));
                        rank=hand[1].substring(0,1);
                        suite=hand[1].substring(1,2);
                        if(rank=="T")
                            rank="10";
                        seatElement.querySelector(".cards > div:last-child .numeral").textContent=rank;
                        seatElement.querySelector(".cards > div:last-child img").setAttribute("src",getSuiteUrl(suite));
                        seatElement.querySelector(".cards > div:last-child img").setAttribute("class",getSuiteClass(suite));
                    }
                }
                //set cash onTable and in pot
                if(playerGameData.amtWon>0){                            
                    seatElement.querySelector(".cashOnTable").innerText="Won $" + playerGameData.amtWon;
                }
                else
                    seatElement.querySelector(".cashOnTable").innerHTML="$" + playerGameData.chipsInPot + "&#x30fb;" + "$" + playerGameData.chipsOnTable;
                //set last action
                seatElement.querySelector(".status").innerText=getPlayerStatusText(player,playerGameData,room,gameEnded);
                //set S or D or B
                var dealerSeatNo=room.getData().dealerSeatNo;
                var smallBlindSeatNo=room.getData().smallBlindSeatNo;
                var bigBlindSeatNo=room.getData().bigBlindSeatNo;
                if(seatNo==smallBlindSeatNo){
                    seatElement.querySelector(".chip div").innerText="S";
                    seatElement.querySelector(".chip").setAttribute("class","chip smallBlind");
                }
                if(seatNo==bigBlindSeatNo){
                    seatElement.querySelector(".chip div").innerText="B";                    
                    seatElement.querySelector(".chip").setAttribute("class","chip bigBlind");
                }
                if(seatNo==dealerSeatNo){
                    seatElement.querySelector(".chip div").innerText="D";                    
                    seatElement.querySelector(".chip").setAttribute("class","chip dealer");
                }                
                //highlight or dim player 
                seatElement.querySelector(".status").classList.remove("action");
                if(!player.isActive()){
                    seatElement.classList.remove("active");
                    seatElement.classList.add("dim");
                }
                else{
                    seatElement.classList.remove("dim");
                    if(player.isCurTurn() && !gameEnded){
                        if(!seatElement.classList.contains("active"))
                            seatElement.classList.add("active");
                    }
                    else
                        seatElement.classList.remove("active");
                }
                
                seatElement.classList.remove("hidden");
                //show hide timer
                if(player.isCurTurn() && !gameEnded && player.getTurnTimeLeftMillis()>5){
                    animateTimer(seatElement,player.getTurnTimeLeftMillis(),Math.floor(player.getTurnTimeLeftMillis()*100/room.turnDurationMillis)+"%");
                }
                else{
                    stopTimerAnimation(seatElement);
                }                
            }
            
            
        }
        
        function getPlayerStatusText(player,playerGameData,room,gameEnded){
            if(gameEnded || player.isCurTurn())
                return player.getName();
            if(playerGameData.lastAction){
                switch (playerGameData.lastAction){
                    case "called": return "Call $" + playerGameData.lastAmtBet;
                    case "folded": return "Fold";
                    case "raised": return "Raise $" + playerGameData.lastAmtBet;
                    case "allin": return "All $" + playerGameData.lastAmtBet;
                    case "smallblind": return "Small Blind";
                    case "bigblind": return "Big Blind";
                    case "checked": return "Check";
                }
                return player.getName();
            }
        }
        
        function getSuiteUrl(suite){
            switch (suite){
                case "c": return clubSrc;
                case "d": return diamondSrc;
                case "s": return spadeSrc;
                case "h": return heartSrc;
            }
            return "";
        }
        
        function getSuiteClass(suite){
            switch (suite){
                case "c": return "club";
                case "d": return "diamond";
                case "s": return "spade";
                case "h": return "heart";
            }
            return "";            
        }
        
        function getTableCardsAsString(room){
           return room.getData().tableCards.toString();
       }

        function getRandomInt(max) {
          return Math.floor(Math.random() * Math.floor(max));
        }

        function println(txt){
            console.log("" + txt);
        }
        
        function stringz(txt){
            if(txt)
                return "";
            return txt;
        }
        
        function fillLocationList(client, gameName, locations){
                var list=document.querySelector("#rooms > ul");
                var listElement;
                var subList;
                var subListElement;
                var location;
                document.querySelector("#lobbyTitle").innerText="Pick a Location";
                document.querySelector("#lobbyTitle").classList.remove("backButton");
                document.querySelector("#lobbyTitle").classList.remove("fakeLink");
                while(list.childNodes.length>0)
                    list.removeChild(list.childNodes[0]);
                
                for(var ctr=0;ctr<locations.length;ctr++){
                    location=locations[ctr];
                    listElement=document.createElement("LI");
                    listElement.setAttribute("class","flexTableRow fakeLink");
                    listElement.addEventListener("click",getRooms.bind(null,location));
                    subList=document.createElement("UL");

                    subListElement=document.createElement("LI");
                    subListElement.setAttribute("class","flexTableCell");
                    subListElement.textContent=location;

                    subList.appendChild(subListElement);
                    listElement.appendChild(subList);                    
                    list.appendChild(listElement);
                }            
        }
        
        function fillRoomList(client, gameName, location, roomtype, rooms){
            var list=document.querySelector("#rooms>ul");
            var listElement;
            var subList;
            var subListElement;
            var room;
            this.lastLocationQueried=location;
            document.querySelector("#lobbyTitle").innerText=location;
            document.querySelector("#lobbyTitle").classList.add("backButton");
            document.querySelector("#lobbyTitle").classList.add("fakeLink");
            while(list.childNodes.length>0)
                list.removeChild(list.childNodes[0]);

            var sliderPopover;
            for(var ctr=0;ctr<rooms.length;ctr++){
                room=rooms[ctr];
                listElement=document.createElement("LI");
                listElement.setAttribute("class","flexTableRow fakeLink");
                listElement.addEventListener("click",joinRoom.bind(null,room.roomId));
                subList=document.createElement("UL");

                subListElement=document.createElement("LI");
                subListElement.setAttribute("class","flexTableCell");
                subListElement.textContent=room.roomName;
                subList.appendChild(subListElement);

                subListElement=document.createElement("LI");
                subListElement.setAttribute("class","flexTableCell");
                subListElement.textContent="$" + room.data.smallBlind + " / $" + room.data.minChipsToTakeSeat;
                subList.appendChild(subListElement);

                subListElement=document.createElement("LI");
                subListElement.setAttribute("class","flexTableCell");
                subListElement.textContent=room.noOfPlayers + " / " + room.totalNoOfSeats;
                subList.appendChild(subListElement);


                listElement.appendChild(subList);                    
                list.appendChild(listElement);                    
            }
            
        }
        
        var lobbyType="";        
        function isInRoomList(){
            return isInLobbyScreen() && lobbyType==="rooms";
        }
        
        function isInLocationList(){
            return isInLobbyScreen() &&  lobbyType==="locations";
        }
        
        function isInLoginScreen(){
            return screenType==="login";
        }
        
        function isInLobbyScreen(){
            return screenType==="lobby";
        }
        
        function isInGameScreen(){
            return screenType==="game";
        }
        
        var screenType="login";
        
        function _animateWinnersStop(callback,seatElement){
            if(areWinnersAnimating())
                animateWinners("stop",callback,seatElement);
        }
        
        function areWinnersAnimating(){
            return document.getElementById("players").querySelector(".winner")!=null;
        }
        
        function _animateLoginIn(direction,callBack){
            screenType="login";
//            document.getElementById("players").classList.add("hidden");
//            document.getElementById("actions").classList.add("hidden");
            hideError();
            hideActionButtons();
            hideAllSeats();
            hideTableCards();
            hidePots();            
            animateLoginIn(direction,function(){
                var mainElement=document.getElementById("main");
                mainElement.classList.remove("lobby");
                mainElement.classList.remove("game");
                mainElement.classList.add("login");
                enableRenderRoom();
                enableTopButton();
                if(callBack)
                    callBack();
            });            
        }
        
        function _animateLobbyIn(direction,callBack){
            screenType="lobby";
//            document.getElementById("players").classList.add("hidden");
//            document.getElementById("actions").classList.add("hidden");
            hideError();
            hideActionButtons();
            hideAllSeats();
            hideTableCards();
            hidePots();
            animateLobbyIn(direction,function(){
                var mainElement=document.getElementById("main");
                mainElement.classList.remove("login");
                mainElement.classList.remove("game");
                mainElement.classList.add("lobby");
                enableRenderRoom();
                enableTopButton();
                if(callBack)
                    callBack();
            });            
        }
        
        function _animateGameIn(callBack,dontAnimate){
            screenType="game";
            adjustForWideAspectRatio();
            var playersElement=document.getElementById("players");
            renderRoom(true);
            if(!dontAnimate){
                animateGameIn(function(){
                    var mainElement=document.getElementById("main");
                    mainElement.classList.remove("login");
                    mainElement.classList.remove("lobby");
                    mainElement.classList.add("game");
                    enableRenderRoom();
                    enableTopButton();
                    if(callBack)
                        callBack();
                });
            }
            else{
                hideAllPlayerPopOvers();                
                var mainElement=document.getElementById("main");
                mainElement.classList.remove("login");
                mainElement.classList.remove("lobby");
                mainElement.classList.add("game");
                enableRenderRoom();
                enableTopButton();
                showGameAfterRejoin();
            }            
        }
        
        function _animateSeatIn(whichSeat,enableRenderRoomAndTopButton){
            animateSeatIn(whichSeat,function(){
                if(enableRenderRoomAndTopButton){
                    enableRenderRoom();
                    enableTopButton();
                }
            });
        }
        
        function _animateLoginOut(callback) {
            animateLoginOut(callback);
        }
        
        function _animateLobbyOut(destination, callback) {
            animateLobbyOut(destination, callback);
        }
        
        function _animateGameOut(callback) {
            animateGameOut(callback);
        }
        
        var onLobbyBackButtonClick=function(){
            if(!isInRoomList())
                return;
            getLocations();
        };
        
        function showError(txt){
            var errElement=document.getElementById("error");
            errElement.querySelector("h2").innerText=txt;
            errElement.classList.remove("hidden");
        }
        
        function hideError(){
            var errElement=document.getElementById("error");
            errElement.querySelector("h2").innerText=" " ;
            errElement.classList.add("hidden");
            
        }
        
        function joinRoom(roomId){
            fullScreen();
            pokerGame.client.joinRoom(roomId);
        }
        
        var onTestButtonClick=function(){
            var prevEmailId=sessionStorage.getItem("pokergame_emailId");
            if(prevEmailId==null)
                prevEmailId="";
            var emailId=window.prompt("enter your email",prevEmailId);
            if(emailId){
                var prevUserName=sessionStorage.getItem("pokergame_userName");
                if(prevUserName==null)
                    prevUserName="";
                var userName=window.prompt("enter your name",prevUserName);
                if(userName){
                    if(pokerGame.isLoggedIn())
                        pokerGame.client.logOut();
                    else
                        pokerGame.registerUser(emailId,"123",userName);
                }
            }
            fullScreen();
        };
        
        var disableTopButtonTime;
        function disableTopButton(){
            disableTopButtonTime=Date.now();            
        }

        function enableTopButton(){
            disableTopButtonTime=0;
        }
        
        var onTopButtonClick=function(){
            if(isAndroid() && document.fullscreenElement==null){
                fullScreen();
                return;
            }
                
            if(Date.now()-disableTopButtonTime<800){
                return;
            }
            disableTopButton();
            if(!pokerGame.isLoggedIn()){
                _animateLoginIn();
                return;
            }
            if(!pokerGame.client.hasJoinedRoom()){
                if(isInRoomList()){
                    getLocations();
                }
                else{
                    pokerGame.client.logOut();
                }
                return;
            }
            if(!pokerGame.client.isSeated()){
                pokerGame.client.leaveRoom();
            }
            else{
                pokerGame.client.leaveSeat();
            }
        };
        
        function showActionButtons(client,gameEnded){
            
            if(!client.isActivePlayer() || gameEnded || client.getPlayer().getData().lastAction=="allIn"){
                disableButton(document.querySelector("#actions .fold"));
                document.querySelector("#actions").classList.add("hidden");
                return;
            }
            enableButton(document.querySelector("#actions .fold"));
            document.querySelector("#actions").classList.remove("hidden");
            if(client.isCurTurn()){
                enableActionButtons(client);
            }
            else{
                disableCallRaiseButtons();
            }            
        }

        function enableActionButtons(client){
            var roomData=client.getJoinedRoom().getData();
            var actionsSection=document.getElementById("actions");
            var callButton=actionsSection.querySelector(".call");
            var raiseButton=actionsSection.querySelector(".raise");

            var callValue=roomData.callValue;
            var minRaiseBet=getMinRaiseBet(roomData);
            var chipsOnTable=client.getPlayer().getData().chipsOnTable;
            if(callValue>chipsOnTable){
                disableButton(callButton);
            }
            else{
                callButton.querySelector(".label").innerText=callValue<1?"Check":"Call";
                callButton.querySelector(".extraLabel").innerText=callValue<1?" ":"$" + callValue;
                enableButton(callButton);
            }
            if(chipsOnTable<1){
                disableButton(callButton);
            }
            else{
                raiseButton.innerText=chipsOnTable>minRaiseBet?"Raise":"All In";
                enableButton(raiseButton);
            }
            actionsSection.querySelector(".popover").classList.add("hidden");
            document.getElementById("main").classList.add("showActions");
        }
                
        function disableButton(buttonElement){
            buttonElement.setAttribute("disabled","disabled");
            buttonElement.classList.add("dim");            
        }
        
        function enableButton(buttonElement){
            buttonElement.removeAttribute("disabled");
            buttonElement.classList.remove("dim");
        }
        
        function isDisabled(buttonElement){
            return buttonElement.hasAttribute("disabled");
        }
        
        function isEnabled(buttonElement){
            return !buttonElement.hasAttribute("disabled");
        }
        
        function disableCallRaiseButtons(){
            var actionsSection=document.getElementById("actions");
            actionsSection.querySelector(".popover").classList.add("hidden");
            actionsSection.querySelector(".timer").classList.add("hidden");
            disableButton(actionsSection.querySelector(".call"));
            disableButton(actionsSection.querySelector(".raise"));
            document.getElementById("main").classList.remove("showActions");
        }
        
        function disableAllActionButtons(){
            var actionsSection=document.getElementById("actions");
            actionsSection.querySelector(".popover").classList.add("hidden");
            actionsSection.querySelector(".timer").classList.add("hidden");
            disableButton(actionsSection.querySelector(".call"));
            disableButton(actionsSection.querySelector(".raise"));
            disableButton(actionsSection.querySelector(".fold"));
            document.getElementById("main").classList.remove("showActions");
            
        }
        
        function hideActionButtons(){
            var actionsSection=document.getElementById("actions");
            document.getElementById("main").classList.remove("showActions");
            actionsSection.querySelector(".popover").classList.add("hidden");
            actionsSection.querySelector(".timer").classList.add("hidden");            
            actionsSection.classList.add("hidden");
        }
        
        function setTopButtonHoverLabel(){
            var labelElement=document.querySelector("#topButton .extraLabel");
            var imgElement=document.querySelector("#topButton img");
            var label="";
            if(!pokerGame.isLoggedIn()){
                if(imgElement.getAttribute("src")!="images/logout.svg")
                    imgElement.setAttribute("src","images/logout.svg");
                labelElement.innerText="";
                return;
            }
            if(isAndroid() && document.fullscreenElement==null){
                if(imgElement.getAttribute("src")!="images/fullscreen-icon.svg")
                    imgElement.setAttribute("src","images/fullscreen-icon.svg");
                labelElement.innerText="Fullscreen";
                return;                
            }
            if(imgElement.getAttribute("src")!="images/logout.svg")
                imgElement.setAttribute("src","images/logout.svg");
            if(!pokerGame.client.hasJoinedRoom()){
                if(isInLocationList()){
                    label="Log Out";
                }
                else{
                    label="Locations";
                }
            }
            else if(!pokerGame.client.isSeated()){
                label="Leave Room";
            }
            else{
                label="Leave Seat";
            }
            if(labelElement.innerText!==label)
                labelElement.innerText=label;
        }
        
        function renderClientInfo(){
            var clientInfoElement=document.getElementById("playerInfoIcon");
            if(!pokerGame.isLoggedIn()){
                clientInfoElement.classList.add("hidden");
                clientInfoElement.querySelector(".totalChips").innerText="";
                return;
            }
            
            var profilePic=pokerGame.client.getProfilePic();
            if(profilePic==null)
                profilePic="images/profile-picture.svg";
            var imgSrc=clientInfoElement.querySelector(".image > img").getAttribute("src");
            if(imgSrc!=profilePic)
                clientInfoElement.querySelector(".image > img").setAttribute("src",profilePic);
            var totChipsTxt="$" + pokerGame.client.gameUserData.chipsInHand;
            if(totChipsTxt!=clientInfoElement.querySelector(".totalChips").innerText)
                clientInfoElement.querySelector(".totalChips").innerText=totChipsTxt;
//            clientInfoElement.classList.remove("hidden");
        }
        
        function setInnerText(element,text){
            element.innerText=text;
        }
        
        var lastInputTime=0;
        function onRangeSliderInput(inputElement){
            var curTime=Date.now();            
            if(curTime-lastInputTime<12)
                return;
            lastInputTime=curTime;

            inputElement.parentNode.querySelector("span input").value=inputElement.value;
        }
        
        function onRangeSliderChange(inputElement){
            inputElement.parentNode.querySelector("span input").value=inputElement.value;
        }
        
        function onTextValueInput(textElement){
            var inputElement=textElement.parentNode.parentNode.querySelector("input[type=\"range\"]");
            if(textElement.value=="" || textElement.value<parseInt(inputElement.min)){
                inputElement.value=inputElement.min;
            }
            else if(textElement.value>parseInt(inputElement.max)){
                textElement.value=inputElement.max;
                inputElement.value=inputElement.max;
            }
            else{
                inputElement.value=textElement.value;
            }
        }
        
        function onTextValueChange(textElement){
            var inputElement=textElement.parentNode.parentNode.querySelector("input[type=\"range\"]");
            if(textElement.value=="" || textElement.value<parseInt(inputElement.min)){
                textElement.value=inputElement.min;
                inputElement.value=inputElement.min;
            }
            else if(textElement.value>parseInt(inputElement.max)){
                textElement.value=inputElement.max;
                inputElement.value=inputElement.max;
            }
            else{
                inputElement.value=textElement.value;
            }
        }
        
        function hideAllPlayerPopOvers(){
            document.getElementById("players").classList.remove("prominent");
            document.querySelectorAll("#players .popover").forEach(function(element){
                element.classList.add("hidden");
            });            
            document.querySelectorAll("#players .player").forEach(function(element){
                element.classList.remove("showPicker");
            });            
        }
        
        function onSeatClick(seatElement){
            //if popover is already visible exit function
            var popover=seatElement.querySelector(".popover");
            if(!popover.classList.contains("hidden"))
                return;
            hideAllPlayerPopOvers();            
            if(!pokerGame.isLoggedIn()){
                return;
            }
            if(pokerGame.client.isSeated()){
                return;
            }
            var seatNo=getSeatNo(seatElement);
            if(seatNo<=0){
                return;
            }
            //check if seat is occupied
            if(!pokerGame.client.getJoinedRoom().isSeatFree(seatNo)){
                return;
            }
            var minChips=pokerGame.client.getJoinedRoom().getData().minChipsToTakeSeat;
            var chipsInHand=pokerGame.client.gameUserData.chipsInHand;
            var rangeInput=popover.querySelector("input[type=\"range\"]");
            var textInput=popover.querySelector("span input");
            if(chipsInHand<minChips){
                //dont take seat
                return;
            }
            //show popover
            rangeInput.setAttribute("min",minChips);
            rangeInput.setAttribute("max",chipsInHand);
            rangeInput.setAttribute("value",minChips);
            rangeInput.value=minChips;
            textInput.min=minChips;
            textInput.max=chipsInHand;
            textInput.value=minChips;
            rangeInput.classList.remove("hidden");
            popover.classList.remove("hidden");
            seatElement.classList.add("showPicker");
//            popover.querySelector("span input").focus();
            document.getElementById("players").classList.add("prominent");
        }
        
        function getRotateBy(){
            if(pokerGame.client.isSeated())
                return middleSeatNo-pokerGame.client.getPlayer().seatNo;
            return 0;
        }
        
        function getRotateBy2(clientSeatNo){
            return middleSeatNo-clientSeatNo;
        }                
        
        function getSeatElement(seatNo){
            initSeatElements();
            seatNo+=getRotateBy();
            if(seatNo>noOfSeats)
                seatNo-=noOfSeats;
            else if(seatNo<1)
                seatNo+=noOfSeats;
            return seatElements[seatNo-1];
        }
        
        function getSeatElement2(seatNo,clientSeatNo){
            initSeatElements();
            seatNo+=getRotateBy2(clientSeatNo);
            if(seatNo>noOfSeats)
                seatNo-=noOfSeats;
            else if(seatNo<1)
                seatNo+=noOfSeats;
            return seatElements[seatNo-1];
        }
        
        function getSeatElementIndex(seatElement){
            for(var ctr=0;ctr<seatElements.length;ctr++){
                if(seatElements[ctr]==seatElement)
                    return ctr;
            }
            return -1;
        }
        
        function getSeatNo(seatElement){
            var ctr;
            var seatNo;
            var rotateBy=getRotateBy();
            for(ctr=0;ctr<seatElements.length;ctr++){
                if(seatElement===seatElements[ctr]){
                    seatNo=ctr+1+rotateBy;
                    if(seatNo>noOfSeats)
                        seatNo-=noOfSeats;
                    else if(seatNo<1)
                        seatNo+=noOfSeats;
                    return seatNo;
                }
            }
            return -1;
        }
        
        function arrayRotate(arr, by) {
            var ctr;
            if(by<0){ 
                by*=-1;
                for(ctr=0;ctr<by;ctr++){
                    arr.push(arr.shift());
                }
            }
            else {
                for(ctr=0;ctr<by;ctr++){
                    arr.unshift(arr.pop());
                }
            }
        }
        
        function chipsOnTableConfirmed(seatElement,evt){
            evt.stopPropagation();
            var popover=seatElement.querySelector(".popover");
            var chips=popover.querySelector("span input").value;
            var seatNo=getSeatNo(seatElement);
            //hide popover            
            document.getElementById("players").classList.remove("prominent");
            seatElement.classList.remove("showPicker");
            popover.classList.add("hidden");
            pokerGame.selectSeat(seatNo,chips);
        }
        
        function onTimerClicked(){
            if(!isAndroid())
                return;
            console.log("on timer click");
            if(isEnabled(document.querySelector(".actionButton.call")))
                onCallClick();
        }
        
        function onTextboxKeyUp(textElement,isActionPopup,event){
            if (event.key === "Enter") {
                event.preventDefault();
                if(!isActionPopup)
                    textElement.parentNode.parentNode.querySelector(".acceptButton").click();
                else
                    document.querySelector(".actionButton.raise").click();
                return;
            }
            else if(event.key === "Escape"){
                event.preventDefault();
                if(!isActionPopup)
                    hideAllPlayerPopOvers();
                else
                    textElement.parentNode.parentNode.classList.add("hidden");
                return;                
            }
        }
        
        function onPopoverMouseUp(popoverElement){
//            popoverElement.querySelector("span input").focus();
        }
    
        function onPageLoad(){
//            if(prevOnLoad)
//                prevOnLoad(); 
//            chipsOnTableSlider=document.querySelector("#lobby .popover");
//            chipsOnTableSlider.querySelector(".closeButton").addEventListener("click",onClosePopOver);
//            showLogin();
            document.querySelector(".actionButton.fold").addEventListener("click",onFoldClick);
            document.querySelector(".actionButton.call").addEventListener("click",onCallClick);
            document.querySelector(".actionButton.raise").addEventListener("click",onRaiseClick);                
            document.querySelector("#actions .allIn").addEventListener("click",onAllIn);                            
//            document.querySelector("#testButton").style.display="none";
            document.querySelector("#testButton").addEventListener("click",onTestButtonClick);
            document.querySelector("#lobbyTitle").addEventListener("click",onLobbyBackButtonClick);
            document.querySelector("#topButton").addEventListener("click",onTopButtonClick);
            document.querySelectorAll("#actions .popover").forEach(function(popoverElement){
                var inputElement=popoverElement.querySelector("input[type=\"range\"]");
                var textElement=popoverElement.querySelector("span input");
                inputElement.addEventListener("input",onRangeSliderInput.bind(null,inputElement));
                inputElement.addEventListener("change",onRangeSliderChange.bind(null,inputElement));
                textElement.addEventListener("input",onTextValueInput.bind(null,textElement));
                textElement.addEventListener("change",onTextValueChange.bind(null,textElement));
                textElement.addEventListener("keyup",onTextboxKeyUp.bind(null,textElement,true));
                popoverElement.addEventListener("mouseup",onPopoverMouseUp.bind(null,popoverElement));                
                popoverElement.classList.add("hidden");
            });
            document.querySelector("#actions .timer").addEventListener("click",onTimerClicked);
            
            var youPlayer=document.querySelector(".player.you");
            var topSection=document.querySelector("#players .top");
            var middleSection=document.querySelector("#players .middle");
            var bottomSection=document.querySelector("#players .bottom");
            youPlayer.classList.remove("you");
            youPlayer.classList.remove("showPicker");

            topSection.innerHTML="";
            topSection.appendChild(youPlayer.cloneNode(true));
            topSection.appendChild(youPlayer.cloneNode(true));
            topSection.appendChild(youPlayer.cloneNode(true));
            topSection.appendChild(youPlayer.cloneNode(true));
            middleSection.innerHTML="";
            middleSection.appendChild(youPlayer.cloneNode(true));
            middleSection.appendChild(youPlayer.cloneNode(true));
            bottomSection.innerHTML="";
            bottomSection.appendChild(youPlayer.cloneNode(true));
            bottomSection.appendChild(youPlayer);
            bottomSection.appendChild(youPlayer.cloneNode(true));
            document.querySelectorAll(".player").forEach(function(seatElement){
                var popoverElement=seatElement.querySelector(".popover");
                var inputElement=popoverElement.querySelector("input[type=\"range\"]");
                var textElement=popoverElement.querySelector("span input");
                inputElement.addEventListener("input",onRangeSliderInput.bind(null,inputElement));
                inputElement.addEventListener("change",onRangeSliderChange.bind(null,inputElement));
                textElement.addEventListener("input",onTextValueInput.bind(null,textElement));
                textElement.addEventListener("change",onTextValueChange.bind(null,textElement));
                textElement.addEventListener("keyup",onTextboxKeyUp.bind(null,textElement,false));
                popoverElement.addEventListener("mouseup",onPopoverMouseUp.bind(null,popoverElement));                
                popoverElement.classList.add("hidden");
                popoverElement.querySelector(".acceptButton").addEventListener("click",chipsOnTableConfirmed.bind(null,seatElement));
                seatElement.addEventListener("click",onSeatClick.bind(null,seatElement));
            });
            document.getElementById("players").addEventListener("click",onPlayersClicked);
            hideActionButtons();
            if(!pokerGame.rejoinSession())
                _animateLoginIn();
        }
        
        function isDescendant(parent, child) {
             var node = child.parentNode;
             while (node != null) {
                 if (node === parent) {
                     return true;
                 }
                 node = node.parentNode;
             }
             return false;
        }

        function isDescendantOfASeatOrIsSeat(child){
            for(var ctr=0;ctr<seatElements.length;ctr++){
                if(child===seatElements[ctr])
                    return true;
                if(isDescendant(seatElements[ctr],child))
                    return true;
            }
            return false;
        }

        function onPlayersClicked(event){
                if(isDescendantOfASeatOrIsSeat(event.target))
                    return;
                hideAllPlayerPopOvers();
            }
        
        function onPageUnload(){
            console.log("in unload");
            if(pokerGame.client!=null)
                pokerGame.client.disconnect();
        }
        
        function isMobileDevice() {
            return /(iPhone)|(iPad)|(iPod)|(Android)/i.test(navigator.userAgent);
        }
        
        function isAndroid(){
            return /(android)/i.test(navigator.userAgent);
        }

    document.addEventListener("DOMContentLoaded", onPageLoad.bind(null));
    window.addEventListener("unload",onPageUnload.bind(null));
    if(isAndroid()){
        document.addEventListener("webkitfullscreenchange",onFullScreenChange);
        document.addEventListener("fullscreenchange",onFullScreenChange);
    }
    
    function onFullScreenChange(){
        if(document.fullscreenElement)
            adjustForWideAspectRatio();
        setTopButtonHoverLabel();
    }
    
    function fullScreen(){
        if(!isMobileDevice())
            return;
        console.log("isMobileDevice");
        var elem=document.documentElement;
        if (elem.webkitRequestFullscreen) {
            elem.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
         } 
        else if (elem.webkitRequestFullScreen) {
            elem.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
         } 
        else if (elem.mozRequestFullscreen) {
            elem.mozRequestFullscreen();
        } 
        else if (elem.requestFullScreen) {
            elem.requestFullScreen();
        } 
        else if (elem.requestFullscreen) {
            elem.requestFullscreen();
        }
    }
 