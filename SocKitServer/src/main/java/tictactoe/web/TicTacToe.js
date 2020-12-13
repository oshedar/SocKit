//Setup Client

var client = new Client("ws://"+document.location.hostname);
var gameName = "TicTacToe";
var singleLocation = "Mumbai";
var turnTimer;
var turnTimerElement;
var turnTimerPlayerIndex = -1;
var winningCells;

function ClientEventHandler() {
	this.onLoggedIn = function (client, isGameSelected){
		client.getRooms( singleLocation, RoomType.normal);
		document.getElementById("backButton").setAttribute("class","");
	};
	
	
        
    this.onGetRooms=function(client, gameName, location, roomtype, rooms) {
		var list = document.querySelector("#rooms ul");
		
		document.getElementById("login").setAttribute("class","hidden");
		document.getElementById("rooms").removeAttribute("class");
		
		while(list.childNodes.length>0)
		    list.removeChild(list.childNodes[0]);
		
		for(var ctr=0;ctr<rooms.length;ctr++){
			room=rooms[ctr];
			listElement=document.createElement("li");
			listElement.textContent = room.roomName;
			listElement.addEventListener("click", joinRoom.bind(null,room.roomId));
			list.appendChild(listElement);
		}
	};

	this.afterServerMessageProcessed = function () {
		//When client joins Room
		if (client.hasJoinedRoom()) {
			renderRoom();
		}
		
	};
	
	this.onSessionRejoined = function (client) {
		document.getElementById("backButton").setAttribute("class","");
		if (client.hasJoinedRoom()) {
			renderRoom();
		}
	};
	
	this.onError = function (client, errorCode, errorDescription) {
		alert(errorDescription);
	};
	
	this.onGamePlayEnded = function (client, room, data) {
		var resultElement = document.getElementById("result");
		var resultTextElement = resultElement.querySelector("h2");
		
		if (data.isDraw) {
			resultTextElement.textContent = "Game Drawn";
		}else {
			resultTextElement.textContent = room.getPlayerBySeatNo(data.winningSeatNo).getName() + " Won!";
		}
		
		winningCells = data.winningCells;
	};
	
	this.onRoomLeft = function (client) {
		document.getElementById("login").setAttribute("class","hidden");
		document.getElementById("game").setAttribute("class","hidden");
		document.getElementById("rooms").removeAttribute("class");
		client.getRooms( singleLocation, RoomType.normal);
	};
	
	this.onLoggedOut = function (client) {
		document.getElementById("rooms").setAttribute("class","hidden");
		document.getElementById("game").setAttribute("class","hidden");
		document.getElementById("login").removeAttribute("class");
		document.getElementById("backButton").setAttribute("class","hidden");
	};
}

client.setClientEventListener(new ClientEventHandler());

//Page Load
document.addEventListener("DOMContentLoaded", function () {
	document.getElementById("loginButton").addEventListener("click", onLoginClicked.bind(null));
	document.getElementById("backButton").addEventListener("click", onBackClicked.bind(null));
	client.rejoinSession();
});

function onBackClicked(event) {
	event.preventDefault();
	
	if (client.hasJoinedRoom()) {
		client.leaveRoom();
	}else {
		client.logOut();
	}
}

function onLoginClicked(event) {
	event.preventDefault();
	
	var email = document.querySelector("#login input[name='email']").value;
	client.registerWithEmailId(email,"noPasswordForDemo", email, gameName);
}

function joinRoom(roomId) {
	client.joinRoom(roomId);
}

function renderRoom() {
	//Setup 
	var room = client.getJoinedRoom();
	var roomData = room.getData();
	var cellElements = document.querySelectorAll("#grid li");
	var playerElements = document.querySelectorAll("#players div h2 em");
	var resultElement = document.getElementById("result");
	var gridElement = document.getElementById("grid");
	
	document.getElementById("login").setAttribute("class","hidden");
	document.getElementById("rooms").setAttribute("class","hidden");
	document.getElementById("game").removeAttribute("class");
		
	//Render the grid	
	for (var counter = 0; counter < roomData.grid.length; counter++) {
		
		cellElements[counter].setAttribute("class", "");
		cellElements[counter].removeEventListener("click", onCellClicked);
                
		switch (roomData.grid[counter]) {
			case "u":
				cellElements[counter].innerHTML = "";
				break;
			case "x":
				cellElements[counter].innerHTML = "&#215;";
				break;
			case "o":
				cellElements[counter].innerHTML = "o";
				break;
		}
	}
	//Enable or disable seats
	for (var seat = 0; seat < 2; seat++) {
		var playerDiv = playerElements[seat].parentElement.parentElement;
		
		if (room.isSeatFree(seat+1)) {
			if (client.isSeated()) {
				playerElements[seat].textContent = "Wating for player...";
				//Disable Click
				playerDiv.removeAttribute("class");
				playerDiv.removeEventListener("click", onPlayerClicked);
			}else {
				playerElements[seat].textContent = "Choose and Play";
				//Enable Click
				playerDiv.setAttribute("class","link");
				playerDiv.addEventListener("click", onPlayerClicked.bind(null, seat));
			}
		}else {
			playerElements[seat].textContent = room.getPlayerBySeatNo(seat+1).getName();
			//Disable Click
			playerDiv.removeAttribute("class");
			playerDiv.removeEventListener("click", onPlayerClicked);
		}
		
		if (room.getCurTurnSeatNo() == seat+1) {
			playerElements[seat].parentElement.parentElement.setAttribute("class", "active");
			if (turnTimerPlayerIndex !== seat) {
				startTurnTimer(seat, room.getCurTurnPlayer().getTurnTimeLeftMillis());
			}
		}
	}
	
	//Enable / Disable cell click based on turn
	for (var counter = 0; counter < roomData.grid.length; counter++) {
            if (client.isCurTurn()) {
                    if (roomData.grid[counter] == "u") {
                            //Enable Cell Click
                            cellElements[counter].setAttribute("class","link");
                            cellElements[counter].addEventListener("click", onCellClicked.bind(null, counter));
                    }
            }
	}
	
	//Show Hide Result
	if (room.isGamePlayEnding()) {
		resultElement.setAttribute("class","");
		gridElement.setAttribute("class", "");
		
		if (winningCells) {
			for (var i = 0; i < winningCells.length; i++) {
				
				cellElements[winningCells[i]].setAttribute("class", "highlight");
			}
		}
		
	}else {
		resultElement.setAttribute("class","hidden");
		gridElement.setAttribute("class", "active");
	}
	
	if (room.isGamePlayEnding() || !room.isGamePlayInProgress()){
		endTurnTimer();
	}
	
}

function onPlayerClicked(seat, event) {
	event.preventDefault();
	client.takeSeat(client.getJoinedRoom().roomId, seat+1, null);
}

function onCellClicked(cellIndex, event) {
	event.preventDefault();
	var data = new Object();
	data.cellIndex = cellIndex;
	client.playAction("cellClicked", data);
}

function startTurnTimer(currentTurnPlayerIndex, turnTime) {
	var playerElements = document.querySelectorAll("#players div .turnTimer");
	var updateSeconds;
	var totalTimeTimer;
	var timeLeft = Math.round(turnTime/1000);
	
	endTurnTimer();
	
	turnTimerElement = playerElements[currentTurnPlayerIndex];
	
	turnTimerElement.setAttribute("class", "turnTimer");
	
	turnTimer = setInterval(function () {
		turnTimerElement.textContent = timeLeft + "s left...";
		timeLeft--;
		if (timeLeft < 0) {
			endTurnTimer();
		}
	}, 1000);
}
function endTurnTimer() {
	if (turnTimer) {
		turnTimerElement.setAttribute("class", "turnTimer hidden");
		clearInterval(turnTimer);
		turnTimerPlayerIndex = -1;
		turnTimer = null;
		turnTimerElement = null;
	}
}

