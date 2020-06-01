function System(){
    
}

System.currentTimeMillis=function(){
  return (new Date()).getTime();  
};

System.isString=function(value){
  return typeof(value) == 'string' || value instanceof String;  
};

System.toHashCode=function(s){
  var h = 0, l = s.length, i = 0;
  if ( l > 0 )
    while (i < l)
      h = (h << 5) - h + s.charCodeAt(i++) | 0;
  return h;    
};


System.debug=function(txt){
//    console.log(txt);
};

System.setCookie=function(cname, cvalue, exdays) {
    if(exdays){
        var d = new Date();
        d.setTime(d.getTime() + (exdays*24*60*60*1000));
        var expires = "expires="+ d.toUTCString();
        document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
    }
    else{
        document.cookie = cname + "=" + cvalue + ";path=/";
    }
};

System.getCookie=function(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
};

System.deleteCookie=function(cname){
    System.setCookie(cname,"",-1);  
};

function initArrayToNull(arr){
    for(var ctr=0;ctr<arr.length;ctr++){
        arr[ctr]=null;
    }
}

function arraycopy(src, srcOffset, dst, dstOffset, length) {
    for(ctr=0;ctr<length;ctr++){
        dst[dstOffset+ctr]=src[srcOffset+ctr];
    }
}

function toUTF8Array(str) {
    if(str==null)
        return null;
    var utf8 = [];
    for (var i=0; i < str.length; i++) {
        var charcode = str.charCodeAt(i);
        if (charcode < 0x80) utf8.push(charcode);
        else if (charcode < 0x800) {
            utf8.push(0xc0 | (charcode >> 6), 
                      0x80 | (charcode & 0x3f));
        }
        else if (charcode < 0xd800 || charcode >= 0xe000) {
            utf8.push(0xe0 | (charcode >> 12), 
                      0x80 | ((charcode>>6) & 0x3f), 
                      0x80 | (charcode & 0x3f));
        }
        // surrogate pair
        else {
            i++;
            charcode = (((charcode&0x3ff)<<10)|(str.charCodeAt(i)&0x3ff)) + 0x010000;
            utf8.push(0xf0 | (charcode >>18), 
                      0x80 | ((charcode>>12) & 0x3f), 
                      0x80 | ((charcode>>6) & 0x3f), 
                      0x80 | (charcode & 0x3f));
        }
    }
    return utf8;
}

function fromUTF8Array(strBytes) {
    if(strBytes==null)
        return null;
    var MAX_SIZE = 0x4000;
    var codeUnits = [];
    var highSurrogate;
    var lowSurrogate;
    var index = -1;

    var result = '';

    while (++index < strBytes.length) {
        var codePoint = Number(strBytes[index]);

        if (codePoint === (codePoint & 0x7F)) {

        } else if (0xF0 === (codePoint & 0xF0)) {
            codePoint ^= 0xF0;
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
        } else if (0xE0 === (codePoint & 0xE0)) {
            codePoint ^= 0xE0;
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
        } else if (0xC0 === (codePoint & 0xC0)) {
            codePoint ^= 0xC0;
            codePoint = (codePoint << 6) | (strBytes[++index] ^ 0x80);
        }

        if (!isFinite(codePoint) || codePoint < 0 || codePoint > 0x10FFFF || Math.floor(codePoint) != codePoint)
            throw RangeError('Invalid code point: ' + codePoint);

        if (codePoint <= 0xFFFF)
            codeUnits.push(codePoint);
        else {
            codePoint -= 0x10000;
            highSurrogate = (codePoint >> 10) | 0xD800;
            lowSurrogate = (codePoint % 0x400) | 0xDC00;
            codeUnits.push(highSurrogate, lowSurrogate);
        }
        if (index + 1 == strBytes.length || codeUnits.length > MAX_SIZE) {
            result += String.fromCharCode.apply(null, codeUnits);
            codeUnits.length = 0;
        }
    }

    return result;
}

function toHexString(byteArray){
    //const chars = new Buffer(byteArray.length * 2);
    const chars = new Uint8Array(byteArray.length * 2);
    const alpha = 'A'.charCodeAt(0) - 10;
    const digit = '0'.charCodeAt(0);

    let p = 0;
    for (let i = 0; i < byteArray.length; i++) {
        let nibble = byteArray[i] >>> 4;
        chars[p++] = nibble > 9 ? nibble + alpha : nibble + digit;
        nibble = byteArray[i] & 0xF;
        chars[p++] = nibble > 9 ? nibble + alpha : nibble + digit;    
    }

    //return chars.toString('utf8');
    return String.fromCharCode.apply(null, chars);
}

function fromHexString(hexString){
    var bytes = new Uint8Array(Math.ceil(hexString.length / 2));
    for (var i = 0; i < bytes.length; i++)
        bytes[i] = parseInt(hexString.substr(i * 2, 2), 16);
    return bytes;
}

function CommandDataSocket(url,connectionListener,commandDataReadListener,encryptor,decryptor){
    this.wsUrl=url;
    this.connectionListener=connectionListener;
    this.commandDataReadListener=commandDataReadListener;
    this.msgQ=new Array();
    this.encryptor=encryptor;
    this.decryptor=decryptor;
    this.ws=null;
    this.openTime=0;
    this.openAttempt=0;
    this.opened=false;
    this.closeCalled=false;
    this.closed=false;

    this.wsonopen=function(){
        if(!this.opened){
            this.opened=true;
            if(this.connectionListener!=null)
                this.connectionListener.socketConnected(this);
        }
        //remove all but last 3 messages from mesg queue
        while(this.msgQ.length>3){
            this.msgQ.shift();
        }
        //send mesgs left To send
        while(this.msgQ.length>0){
            this.ws.send(this.msgQ.shift());
        }        
    };

    this.wsonmessage = function (evt) {
        var bytes=evt.data==null?null:new Uint8Array(evt.data);
        if(bytes==null || this.isClosed() || this.commandDataReadListener==null)
            return;
        if(this.decryptor!=null) 
            bytes=this.decryptor.decrypt(bytes);
        var commandBytesLength=bytes[0] & 127;
        var isText=(bytes[0] & 128)==128;
        var command=fromUTF8Array(bytes.slice(1,commandBytesLength+1));
        var data=bytes.length>commandBytesLength+1?bytes.slice(commandBytesLength+1, commandBytesLength+1+bytes.length):null;
        if(isText){
            commandDataReadListener.commandDataRead(this, command,data==null?null:fromUTF8Array(data));
        }
        else{
            commandDataReadListener.commandDataReadBytes(this,command,data);
        }

    };

    this.wsonclose = function() {
        this.onClose(null);
    };

    this.isClosed=function(){
      return this.closed;
    };

    this.onClose=function(err){
        if(this.closed)
            return;
        if(!this.closeCalled && !this.opened && (this.openTime-Date.now())<10000 && this.openAttempt<3){
            this.createWebSocket();
            return;
        }
        if(this.closed)
            return;
        while(this.msgQ.length>0)
            this.msgQ.pop();
        this.closed=true;
        if(this.connectionListener!=null){
            if(this.closeCalled)
                this.connectionListener.socketClosed(this);
            else if(this.opened)
                this.connectionListener.socketDisconnected(this);
            else
                this.connectionListener.connectionFailed(this,err);
        }
    };

    this.createWebSocket=function(){
        if(this.closeCalled||this.opened)
            return;
        if(this.openTime==0){
            this.openTime=Date.now();
            if(this.connectionListener!=null)
                this.connectionListener.socketConnecting(this);
        }
        this.openAttempt++;
        try{
            this.ws=new WebSocket(this.wsUrl);
            this.ws.binaryType="arraybuffer";
        }catch(err){
            this.onClose(err);
            return;
        }
        this.ws.addEventListener("open",this.wsonopen.bind(this));
        this.ws.addEventListener("message",this.wsonmessage.bind(this));
        this.ws.addEventListener("close",this.wsonclose.bind(this));        
    };

    this.createWebSocket();

    this.close=function(){
        var wsObj=this.ws;
        if(this.closed)
            return;
        this.ws=null;
        this.closeCalled=true;
        if(wsObj!=null && wsObj.readyState<2)
            wsObj.close();
        else
            this.onClose(null);
    };

    this.setCommandDataReadListener=function(commandDataReadListener){
        this.commandDataReadListener=commandDataReadListener;
    };
    
    this.setEncryptorDecryptor=function(encryptor,decryptor){
        this.encryptor=encryptor;
        this.decryptor=decryptor;
    };

    this.write=function(command,data){
        if(this.closed)
            throw new Error("socket is closed");
        if(data==null || typeof data=="string"){
            System.debug("mesg sent: " + command);
            this.writeBytes(command,toUTF8Array(data),true);
        }
        else {
            this.writeBytes(command,data,false);
        }
    };

    this.writeBytes=function(command,data,isText){
        var commandBytes=toUTF8Array(command);
        var bytes=new Uint8Array(commandBytes.length+1 + (data==null?0:data.length));
        bytes[0]=(commandBytes.length | (isText?128:0));
        arraycopy(commandBytes, 0, bytes, 1, commandBytes.length);
        if(data!=null)
            arraycopy(data, 0, bytes, commandBytes.length+1, data.length);
        if(encryptor!=null)
            bytes=encryptor.encrypt(bytes);                
        
        if(this.ws===null){
            this.msgQ.push(bytes);
            this.createWebSocket();
            return;
        }
        if(this.ws.readyState==1){
            this.ws.send(bytes);
        }
        else {
            this.msgQ.push(bytes);
        }
    };

    this.isLocal=function(){
        return false;
    };

}

function Room(ownerUserId,location,roomType,gameName,roomId,roomName,totalNoOfSeats,turnDurationMillis){
    this.gameName = gameName;
    this.location = location;
    this.roomId = roomId;
    this.roomName = roomName;
    this.totalNoOfSeats = totalNoOfSeats;
    this.turnDurationMillis = turnDurationMillis;
    this.roomType = roomType;
    this.ownerUserId = ownerUserId == -1 ? null : ownerUserId;
    this.gameInProgress=false;
    this.gameEnding=false;
    this.gameNo=0;
    this.gameData;
    this.curTurnSeatNo=0;
    this.seats = new Array(totalNoOfSeats);
    this.occupiedCount = 0;
    
    this.isGamePlayInProgress=function() {
        return this.gameInProgress;
    };

    this.isGamePlayEnding=function() {
        return this.gameEnding;
    };

    this.isPrivate=function() {
        return this.ownerUserId != null;
    };

    this.isOwner=function(userId) {
        if (userId == null || this.ownerUserId == null) {
            return false;
        }
        return userId==this.ownerUserId;
    };

    this.getData=function() {
        return this.gameData;
    };

    this.refreshRoomData=function(jsonObject,client) {
        var playersAsJson = new Array(this.totalNoOfSeats + 1);
        this.gameInProgress = jsonObject.gameInProgress;
        this.gameEnding = jsonObject.gameEnding;
        this.gameNo = jsonObject.gameNo;
        this.gameData = jsonObject.data;
        this.curTurnSeatNo = jsonObject.curTurnSeatNo;
        var player;
        var playerAsJson;
        var seatNo;
        for (var ctr=0;ctr<jsonObject.players.length;ctr++) {
            playerAsJson = jsonObject.players[ctr];
            seatNo = playerAsJson.seatNo;
            playersAsJson[seatNo] = playerAsJson;
        }
        for (var ctr = 1; ctr < playersAsJson.length; ctr++) {
            if (playersAsJson[ctr] == null) {
                this.removePlayer(ctr);
                continue;
            }
            player = this.getPlayerBySeatNo(ctr);
            if (player == null) {
                this.set(ctr, Player.newPlayer(playersAsJson[ctr], client,this));
                continue;
            }
            if (playersAsJson[ctr].userId == player.userId) {
                player.refreshPlayerData(playersAsJson[ctr], client,this);
            } else {
                this.set(ctr, Player.newPlayer(playersAsJson[ctr], client,this));
            }
        }
    };
    
    this.getGamePlayNo=function(){
        return this.gameNo;
    };

    this.setData=function(data){
        this.gameData=data;
    };
    
    this.isSeated=function(userId) {
        var player;
        for (var ctr=0;ctr<this.seats.length;ctr++) {
            player=this.seats[ctr];
            if (player != null && player.userId == userId) {
                return true;
            }
        }
        return false;
    };

    this.isCurTurn=function(player) {
        return this.gameInProgress && this.curTurnSeatNo == player.seatNo && this.getPlayerBySeatNo(player.seatNo) == player;
    };

    this.getCurTurnSeatNo=function() {
        if (!this.gameInProgress) {
            return 0;
        }
        return this.curTurnSeatNo;
    };

    this.getCurTurnPlayer=function() {
        if (!this.gameInProgress || this.curTurnSeatNo < 1) {
            return null;
        }
        return this.getPlayerBySeatNo(this.curTurnSeatNo);
    };

    this.getPlayerByUserId=function(userId) {
        var player;
        for (var ctr=0;ctr<this.seats.length;ctr++) {
            player=this.seats[ctr];
            if (player != null && player.userId == userId) {
                return player;
            }
        }
        return null;
    };

    this.getPlayerCount=function() {
        return this.occupiedCount;
    };

    this.clear=function() {
        for (var ctr = 0; ctr < this.seats.length; ctr++) {
            this.seats[ctr] = null;
        }
        this.occupiedCount = 0;
    };

    this.set=function(seatNo,player) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new Error("Invalid seatNo");
        }
        var oldPlayer = this.seats[seatNo - 1];
        if (player == null) {
            if (oldPlayer != null) {
                this.occupiedCount--;
            }
            this.seats[seatNo - 1] = null;
            return oldPlayer;
        }
        if (oldPlayer == null) {
            this.occupiedCount++;
        }
        this.seats[seatNo - 1] = player;
        return oldPlayer;
    };

    this.removePlayer=function(seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new Error("Invalid seatNo");
        }
        var player = this.seats[seatNo - 1];
        this.seats[seatNo - 1] = null;
        if (player != null) {
            this.occupiedCount--;
        }
        return player;
    };

    this.remove=function(player) {
        if (player == null) {
            throw new Error("player is null");
        }
        for (var ctr = 0; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] == player) {
                this.seats[ctr] = null;
                this.occupiedCount--;
                return player;
            }
        }
        return null;
    };

    this.getPlayerBySeatNo=function(seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new Error("Invalid seatNo");
        }
        return this.seats[seatNo - 1];
    };

    this.getFreeSeatNo=function() {
        for (var ctr = 0; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] == null) {
                return ctr + 1;
            }
        }
        return 0;
    };

    this.isSeatFree=function(seatNo) {
        if (seatNo < 1 || seatNo > this.seats.length) {
            throw new Error("Invalid seatNo");
        }
        return this.seats[seatNo - 1] == null;
    };

    this.getNextPlayer=function(prevSeatNo) {
        if (prevSeatNo < 1 || prevSeatNo > this.seats.length) {
            throw new Error("Invalid seatNo");
        }
        for (var ctr = prevSeatNo; ctr < this.seats.length; ctr++) {
            if (this.seats[ctr] != null) {
                return this.seats[ctr];
            }
        }
        for (var ctr = 0; ctr < prevSeatNo - 1; ctr++) {
            if (this.seats[ctr] != null) {
                return this.seats[ctr];
            }
        }
        return null;
    };
    
    this.getNextPlayer=function(prevPlayer){
        if (prevPlayer.seatNo > 0 && this.seats[prevPlayer.seatNo-1]==prevPlayer) {
            return this.getNextPlayer(prevPlayer.seatNo);
        }
        return null;        
    };

    this.getNextOccupantFromPlayer=function(player) {
        var seatNo = this.getPlayerSeatNo(player);
        if (seatNo > 0) {
            return this.getNextPlayer(seatNo);
        }
        return null;
    };
    
    this.getPlayers=function(){
      var players=new Array();
      for(var ctr=0;ctr<this.seats.length;ctr++){
          if(this.seats[ctr]!=null)
              players.push(this.seats[ctr]);
      }
      return players;
    };
    
}

Room.newRoom=function(jsonData,client){
    var room=new Room(jsonData.ownerUserId,jsonData.location,jsonData.roomType,jsonData.gameName,jsonData.roomId,jsonData.roomName,jsonData.totalNoOfSeats,jsonData.turnDurationMillis);
    room.refreshRoomData(jsonData,client);
    return room;    
};

function Player(userId, seatNo) {
    this.userId = userId;
    this.name = null;
    this.avtarId = 0;
    this.useProfilePicture = false;
    this.seatNo = seatNo;
    this.inGame = false;
    this.turnTimeLeftMillis = 0;
    this.whenTurnTimeLeftMillisWasSet=System.currentTimeMillis();
    this.gameData = null;
    this.profilePic=null;
    this.room=null;

    this.refreshPlayerData=function(jsonObject,client,room){
        this.name=jsonObject.name;
        this.avtarId=jsonObject.avtarId;
        this.useProfilePicture=jsonObject.useProfilePicture;
        this.profilePic=jsonObject.profilePic;
        this.inGame=jsonObject.inGame;
        this.turnTimeLeftMillis=jsonObject.turnTimeLeftMillis;
        this.whenTurnTimeLeftMillisWasSet=System.currentTimeMillis();
        this.gameData=jsonObject.gameData;
        this.room=room;
    };
    
    this.getName=function() {
        return this.name;
    };

    this.getAvtarId=function() {
        return this.avtarId;
    };

    this.shouldUseProfilePicture=function () {
        return this.useProfilePicture;
    };
    
    this.getProfilePic=function(){
        return this.profilePic;
    };

    this.isActive=function () {
        return this.inGame;
    };

    this.getTurnTimeLeftMillis=function () {
        var timeDiff=System.currentTimeMillis()-this.whenTurnTimeLeftMillisWasSet;
        if(timeDiff>this.turnTimeLeftMillis)
            return 0;
        return this.turnTimeLeftMillis-timeDiff;
//        return this.turnTimeLeftMillis;
    };

    this.getData=function () {
        return this.gameData;
    };
    
    this.isCurTurn=function (){
        return this.room.isCurTurn(this);
    };

    this.isSameAsClient=function(client){
        return this.userId==client.userId;
    };
}

Player.newPlayer=function(jsonObject,client,room){
    var player=new Player(jsonObject.userId, jsonObject.seatNo);
    player.refreshPlayerData(jsonObject,client,room);
    return player;
};
    
function ErrorCodes(){
    
}
ErrorCodes.duplicateEmailId=1;
ErrorCodes.invalidEmailId=2;
ErrorCodes.emailIdAndPasswordDoesNotMatch=4;
ErrorCodes.invalidLoginData=5;
ErrorCodes.invalidGameName=6;
ErrorCodes.invalidLocation=7;
ErrorCodes.seatNotFree=8;
ErrorCodes.noRoomJoined=9;
ErrorCodes.roomIdDoesNotExist=10;
ErrorCodes.shutdownStarted=11;
ErrorCodes.roomDestroyed=12;
ErrorCodes.invalidSeatNo=13;
ErrorCodes.inElligibleToTakeSeat=14;
ErrorCodes.noGameEntered=15;
ErrorCodes.tooManySessions=16;
ErrorCodes.tooManyUsersLoggedIn=17;
ErrorCodes.isStillInRoom=18;
ErrorCodes.invalidLoginType=19;
ErrorCodes.invalidRegistrationType=20;
ErrorCodes.emptyEmailId=21;
ErrorCodes.emptyOtherId=22;
ErrorCodes.duplicateOtherId=23;
ErrorCodes.otherIdAndPasswordDoesNotMatch=24;

function RoomType(){
    
}
RoomType.normal="normal";
RoomType.fast="fast";

function LoginType(){
}
LoginType.email="email";
LoginType.other="other";
LoginType.google="google";

function ServerMessageType(){
    
}
ServerMessageType.roomData=System.toHashCode("roomData");
ServerMessageType.sessionTimedOut=System.toHashCode("sessionTimedOut");
ServerMessageType.sessionRejoined=System.toHashCode("sessionRejoined");
ServerMessageType.roomJoined=System.toHashCode("roomJoined");
ServerMessageType.seatTaken=System.toHashCode("seatTaken");
ServerMessageType.notEligibeToPlay=System.toHashCode("notElligibeToPlay");
ServerMessageType.gamePlayStarted=System.toHashCode("newGame");
ServerMessageType.nextTurn=System.toHashCode("nextTurn");
ServerMessageType.gameAction=System.toHashCode("gameAction");
ServerMessageType.invalidAction=System.toHashCode("invalidAction");
ServerMessageType.outOfTurnPlayed=System.toHashCode("outOfTurnPlayed");
ServerMessageType.turnPlayed=System.toHashCode("turnPlayed");
ServerMessageType.gamePlayEnded=System.toHashCode("endGame");
ServerMessageType.seatLeft=System.toHashCode("seatLeft");
ServerMessageType.roomLeft=System.toHashCode("roomLeft");
ServerMessageType.roomDestroyed=System.toHashCode("roomDestroyed");
ServerMessageType.loggedOut=System.toHashCode("loggedOut");
ServerMessageType.locations=System.toHashCode("locations");
ServerMessageType.roomList=System.toHashCode("rooms");
ServerMessageType.avtarChanged=System.toHashCode("avtarChanged");
ServerMessageType.serverShutDown=System.toHashCode("shutDown");
ServerMessageType.error=System.toHashCode("error");
ServerMessageType.loggedIn=System.toHashCode("loggedIn");
ServerMessageType.userData=System.toHashCode("userData");
ServerMessageType.gameEntered=System.toHashCode("gameSelected");
ServerMessageType.gameExited=System.toHashCode("gameDeselected");
ServerMessageType.customMessage=System.toHashCode("customMessage");
ServerMessageType.toEnum=function(command){
    if(command.startsWith("#"))
        return System.toHashCode(command.substring(1, command.length-1));
    return System.toHashCode("customMessage");
};
ServerMessageType.getCustomCommand=function(command){
    if(command.startsWith("#"))
        return null;
    return command.substring(2,command.length-2);
};

function  ShortRoom(roomId,roomName,totalNoOfSeats,noOfPlayers,data) {
    this.roomId=roomId;
    this.roomName = roomName;
    this.totalNoOfSeats=totalNoOfSeats;
    this.noOfPlayers = noOfPlayers;
    this.data = data;
}

function Client(url) {
    this.wsUrl = url;
    this.userId=null;
    this.sessionId=null;
    this.name=null;
    this.avtarId=0;
    this.emailId=null;
    this.password=null;
    this.otherId=null;
    this.loginType=null;
    this.profilePic=null;
    this.gameName=null;
    this.gameUserData=null;
    this.socket=null;
    this.loggedIn=false;
    this.clientEventListener=null;
    this.lastSentTime=0;
    this.poller=null;
    
    this.joinedRoom=null;
    
    this.lastPlayAction=null;
    this.lastPlayActionData=null;
        
    this.registerWithEmailId=function(emailId,password,name,gameName){
        var jsonObject;
        if(this.loggedIn)
            this.logOut();            
        this.reset();
        if(emailId!=null)
            emailId=emailId.trim();
        if(emailId==null || emailId.length<1)
            throw new Error("EmailId is null or 0 length String");
        if(password==null || password.length<1)
            throw new Error("Password is null or 0 length String");
        if(name!=null)
            name=name.trim();
        if(name==null || name.length<1)
            throw new Error("Name is null or 0 length String");
        this.emailId=emailId;
        this.password=password;
        this.name=name;
        jsonObject=new Object();
        jsonObject.type="email";
        jsonObject.emailId=emailId;
        jsonObject.password=password;
        jsonObject.name=name;
        jsonObject.gameName=gameName;
        this.sendMesg("#register#",JSON.stringify(jsonObject));
    };    
    
    this.registerWithOtherId=function(otherId,password,name,gameName){
        var jsonObject;
        if(this.loggedIn)
            this.logOut();            
        this.reset();
        if(otherId!=null)
            otherId=otherId.trim();
        if(otherId==null || otherId.length<1)
            throw new Error("otherId is null or 0 length String");
        if(password==null || password.length<1)
            throw new Error("Password is null or 0 length String");
        if(name!=null)
            name=name.trim();
        if(name==null || name.length<1)
            throw new Error("Name is null or 0 length String");
        this.otherId=otherId;
        this.password=password;
        this.name=name;
        jsonObject=new Object();
        jsonObject.type="other";
        jsonObject.emailId=otherId;
        jsonObject.password=password;
        jsonObject.name=name;
        jsonObject.gameName=gameName;
        this.sendMesg("#register#",JSON.stringify(jsonObject));
    };    
    
    this.logInWithEmailId=function(emailId,password,gameName){
        var jsonObject=new Object();
        if(this.loggedIn)
            this.logOut();            
        this.reset();
        jsonObject.type="email";
        jsonObject.emailId=emailId;
        jsonObject.password=password;
        jsonObject.gameName=gameName;
        this.emailId=emailId;
        this.password=password;
        this.loginType=LoginType.email;
        this.sendMesg("#login#",JSON.stringify(jsonObject));
    };

    this.logInWithGoogle=function(idToken,gameName){
        var jsonObject=new Object();
        if(this.loggedIn)
            this.logOut();            
        this.reset();
        jsonObject.type="google";
        jsonObject.idToken=idToken;
        jsonObject.gameName=gameName;
        this.loginType=LoginType.google;
        this.sendMesg("#login#",JSON.stringify(jsonObject));
    };

    this.logInWithOtherId=function(otherId,password,gameName){
        if(this.loggedIn)
            logOut();            
        this.reset();
        var jsonObject=new Object();
        jsonObject.type="other";
        jsonObject.otherId=otherId;
        jsonObject.password=password;
        jsonObject.gameName=gameName;
        this.otherId=otherId;
        this.password=password;
        this.loginType=LoginType.other;
        this.sendMesg("#login#",JSON.stringify(jsonObject));
    };
    
    this.isConnected=function(){
      return this.socket!=null && this.socket.isClosed()==false;  
    };
    
    this.disconnect=function(){
        if(this.socket!=null){
            if(!this.socket.isClosed())
                this.socket.close();
            this.socket=null;
        }
    };
    
    //resets client data before register or login
    this.reset=function(){        
        if(this.socket!=null){
            this.socket.close();
            this.socket=null;
        }
        this.userId=null;
        this.sessionId=null;
        this.name=null;
        this.avtarId=0;
        this.emailId=null;
        this.password=null;
        this.otherId=null;
        this.loginType=null;
        this.loggedIn=false;
        this.poller=null;
        this.joinedRoom=null;
        this.gameName=null;
        this.gameUserData=null;
    };

    this.sendJsonMessage=function(command,jsonData){
        if(!this.loggedIn || command==null)
            return;
        if(jsonData==null )
            jsonData=new Object();
        this.sendMesg("__" + command,JSON.stringify(jsonData));
    };

    this.sendTxtMessage=function(command,data){
        if(!this.loggedIn || command==null)
            return;
        this.sendMesg("--" + command,data);
    };

    this.sendBinaryMessage=function (command,data){
        if(!this.loggedIn || command==null)
            return;
        this.sendMesg("--" + command,data);        
    };
    
    this.pollData=null;

    this.getPollData=function(){
        var pollData=this.pollData;
        var jsonObject=null;
        if(pollData!=null)
            return pollData;
        jsonObject=new Object();
        jsonObject.sessionId=this.sessionId;
        pollData=JSON.stringify(jsonObject);
        this.pollData=pollData;
        return pollData;
    };

    this.sendMesg=function(command,data){
        try{
            if(this.socket==null || this.socket.isClosed()){
                this.socket=new CommandDataSocket(this.wsUrl, this,this,null,null);
                //if command is not register or login or poll or rejoinSession then send poll mesg first
                if(!("#poll#"===command || "#login#"===command || "#register#"===command || "#rejoinSession#"===command)){
                    this.socket.write("#poll#", this.getPollData());
                }

            }
            if(this.socket.write(command, data)){
                this.lastSentTime=System.currentTimeMillis();
            }            
        }
        catch(error){
            console.error(error);
        }
    };
    
    this.changeAvtarId=function (avtarId){
        var jsonObject=null;
        if(this.avtarId==avtarId)
            return;
        if(!this.loggedIn)
            return;
        jsonObject=new Object();
        jsonObject.avtarId=avtarId;
        this.sendMesg("#changeAvtar#", JSON.stringify(jsonObject));
    };
    
    this.logOut=function (){
            if(!this.loggedIn)
                return;
            //send logOut mesg
            this.sendMesg("#logout#", null);
    };
    
    this.getLocations=function(){
        var jsonObject=null;
        if(!this.loggedIn)
            return;
        this.sendMesg("#getLocations#", null);
    };

    this.getRooms=function(location,roomType){
        var jsonObject=null;
        if(!this.loggedIn)
            return;
        jsonObject=new Object();
        jsonObject.location=location;
        jsonObject.roomType=roomType;
        this.sendMesg("#getRooms#", JSON.stringify(jsonObject));
    };
    
    this.joinRoom=function(roomId){
        var jsonObject=null;
        if(!this.loggedIn)
            return;
        jsonObject=new Object();
        jsonObject.roomId=parseInt("" + roomId);
        this.sendMesg("#joinRoom#", JSON.stringify(jsonObject));
    };
    
    this.refreshRoomFromServer=function(){
        if(!this.loggedIn)
            return;
        if(this.joinedRoom!=null)
            this.sendMesg("#getRoomData#", null);
    };
    
    this.takeSeat=function(roomId,seatNo,data){
        var jsonObject=null;
        if(!this.loggedIn)
            return;            
        jsonObject=new Object();
        jsonObject.roomId=roomId;
        jsonObject.seatNo=seatNo;
        if(data==null)
            data=new Object();
        jsonObject.data=data;
        this.sendMesg("#takeSeat#", JSON.stringify(jsonObject));
    };
    
    this.leaveSeat=function(){
        if(!this.loggedIn)
            return;
        if(this.isSeated())
            this.sendMesg("#leaveSeat#", null);
    };
    
    this.leaveRoom=function(){
        if(!this.loggedIn || this.joinedRoom==null){
            alert("joined room is null");
            return;
        }
        this.sendMesg("#leaveRoom#", null);        
    };
        
    this.commandDataRead=function(socket,command,data){
        var dataAsJson=null;
        
        System.debug("mesg recd: " + command + ", " + data);
        
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.beforeServerMessageProcessed === "function")
                setTimeout(this.clientEventListener.beforeServerMessageProcessed.bind(this.clientEventListener,this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command), false,data),0);
        if(data!=null){
            try{
                dataAsJson=JSON.parse(data);
            }catch(error){}
        }
        switch(command){
            case "#loggedIn#":
                this.userId=dataAsJson.userId;
                this.sessionId=dataAsJson.sessionId;
                this.name=dataAsJson.name;
                this.avtarId=dataAsJson.avtarId;
                this.profilePic=dataAsJson.profilePic;
                this.gameName=dataAsJson.gameName;
                this.gameUserData=dataAsJson.gameUserData;
                this.loggedIn=true;
                this.startPoller();
                sessionStorage.setItem("gameClient_sessionId",this.sessionId);                
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onLoggedIn === "function")
                        setTimeout(this.clientEventListener.onLoggedIn.bind(this.clientEventListener,this,this.gameName!=null),0);
                break;
            case "#sessionRejoined#":
                var userJson=dataAsJson.user;
                var roomJson=dataAsJson.room;
                this.userId=userJson.userId;
                this.sessionId=userJson.sessionId;
                this.name=userJson.name;
                this.avtarId=userJson.avtarId;
                this.profilePic=userJson.profilePic;
                this.gameName=userJson.gameName;
                this.gameUserData=userJson.gameUserData;
                this.loggedIn=true;
                this.joinedRoom=(roomJson===undefined || roomJson===null)?null:Room.newRoom(roomJson,this);
                this.startPoller();
                sessionStorage.setItem("gameClient_sessionId",this.sessionId);                
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onSessionRejoined === "function")
                        setTimeout(this.clientEventListener.onSessionRejoined.bind(this.clientEventListener,this),0);
                break;
            case "#error#":{
                var errorCode=dataAsJson.code;
                if(this.clientEventListener!=null){
                    if(errorCode==ErrorCodes.shutdownStarted){
                        if(typeof this.clientEventListener.onServerShutdown === "function")
                            setTimeout(this.clientEventListener.onServerShutdown.bind(this.clientEventListener,this),0);
                    }
                    else
                        if(typeof this.clientEventListener.onError === "function")
                           setTimeout(this.clientEventListener.onError.bind(this.clientEventListener,this,errorCode, dataAsJson.desc),0);
                }
                break;
            }
            case "#loggedOut#":
                this.loggedIn=false;
                this.reset();
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onLoggedOut === "function")
                        setTimeout(this.clientEventListener.onLoggedOut.bind(this.clientEventListener,this),0);
                break;
            case "#sessionTimedOut#":
                this.loggedIn=false;
                this.reset();
                sessionStorage.removeItem("gameClient_sessionId");
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onSessionTimedOut === "function")
                        setTimeout(this.clientEventListener.onSessionTimedOut.bind(this.clientEventListener,this),0);
                break;
            case "#shutDown#":
                this.loggedIn=false;
                this.reset();
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onServerShutdown === "function")
                        setTimeout(this.clientEventListener.onServerShutdown.bind(this.clientEventListener,this),0);
                break;
            case "#gameSelected#":
                {
                    this.gameName=dataAsJson.gameName;
                    this.gameUserData=dataAsJson.gameUserData;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGameSelected === "function")
                            setTimeout(this.clientEventListener.onGameSelected.bind(this.clientEventListener,this,this.gameName),0);
                }
                break;
            case "#gameDeselected#":
                {
                    var oldGameName=this.gameName;
                    this.gameName=null;
                    this.gameUserData=null;
                    if(oldGameName!=null && this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGameDeselected === "function")
                            setTimeout(this.clientEventListener.onGameDeselected.bind(this.clientEventListener,this,oldGameName),0);
                }
                break;
            case "#locations#":
                if(!this.loggedIn)
                    return;
                {
                    var locations=null;
                    var gameName=dataAsJson.gameName;
                    var jsonArray=dataAsJson.locations;
                    locations=new Array();
                    for(var ctr=0;ctr<jsonArray.length;ctr++){
                        locations.push(jsonArray[ctr]);
                    }
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGetLocations === "function")
                            setTimeout(this.clientEventListener.onGetLocations.bind(this.clientEventListener,this,gameName,locations),0);
                }
                break;
            case "#rooms#":
                if(!this.loggedIn)
                    return;
                {
                    var gameName=dataAsJson.gameName;
                    var location=dataAsJson.location;
                    var roomType=dataAsJson.roomType;
                    var roomsAsJson=dataAsJson.rooms;
                    var rooms=new Array();
                    var jsonObject;
                    for(var ctr=0;ctr<roomsAsJson.length;ctr++){
                        jsonObject=roomsAsJson[ctr];
                        rooms.push(new ShortRoom(jsonObject.roomId, jsonObject.roomName,jsonObject.totalNoOfSeats, jsonObject.noOfPlayers, jsonObject.data));
                    }                                
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGetRooms === "function")
                            setTimeout(this.clientEventListener.onGetRooms.bind(this.clientEventListener,this, gameName, location, roomType, rooms),0);
                }
                break;
            case "#roomJoined#":
                this.lastPlayAction=null;
                if(!this.loggedIn)
                    return;
                this.gameUserData=dataAsJson.gameUserData;
                this.joinedRoom=Room.newRoom(dataAsJson.room,this);
                if(this.clientEventListener!=null){
                    if(typeof this.clientEventListener.onRoomJoined === "function")
                        setTimeout(this.clientEventListener.onRoomJoined.bind(this.clientEventListener,this, this.joinedRoom),0);
               }
                break;
            case "#roomData#":
                if(!this.loggedIn){
                    this.lastPlayAction=null;
                    return;
                }
                {
                    var roomId=dataAsJson.roomId;
                    if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                        this.joinedRoom=Room.newRoom(dataAsJson,this);
                    else
                        this.joinedRoom.refreshRoomData(dataAsJson,this);                            
                    if(this.isCurTurn() && this.lastPlayAction!=null)
                        this.playAction(this.lastPlayAction, this.lastPLayActionData);
                    else
                        this.lastPlayAction=null;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onRoomRefreshedFromServer === "function")
                            setTimeout(this.clientEventListener.onRoomRefreshedFromServer.bind(this.clientEventListener,this, this.joinedRoom),0);
                }
                break;
            case "#seatTaken#":
                if(!this.loggedIn || this.joinedRoom==null){
                    this.lastPlayAction=null;
                    return;
                }
                var playerSeated=Player.newPlayer(dataAsJson.player,this,this.joinedRoom);
                this.joinedRoom.set(playerSeated.seatNo,playerSeated);
                if(this.userId===playerSeated.userId){
                    this.lastPlayAction=null;
                    this.gameUserData=dataAsJson.gameUserData;
                }
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onSeatTaken === "function")
                        setTimeout(this.clientEventListener.onSeatTaken.bind(this.clientEventListener,this,playerSeated, this.joinedRoom,playerSeated.userId==this.userId),0);
                break;
            case "#userData#":
                this.gameUserData=dataAsJson;
                break;
            case "#seatLeft#":
                if(!this.loggedIn || this.joinedRoom==null){
                    this.lastPlayAction=null;
                    return;
                }
                {
                    var seatNo=dataAsJson.seatNo;
                    var userId=dataAsJson.userId;
                    var playerLeft=this.joinedRoom.removePlayer(seatNo);                    
                    if(userId==this.userId)
                        this.gameUserData=dataAsJson.gameUserData;
                    if(this.userId===userId)
                        this.lastPlayAction=null;
                    else if(playerLeft!=null && this.userId===playerLeft.userId)
                        this.lastPlayAction=null;
                    if(playerLeft!=null && this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onSeatLeft === "function")
                            setTimeout(this.clientEventListener.onSeatLeft.bind(this.clientEventListener,this,playerLeft, this.joinedRoom,playerLeft.userId==this.userId),0);
                }
                break;
            case "#roomLeft#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                this.gameUserData=dataAsJson.gameUserData;
                this.joinedRoom=null;
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onRoomLeft === "function")
                        setTimeout(this.clientEventListener.onRoomLeft.bind(this.clientEventListener,this),0);
                break;
            case "#newGame#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var roomId=dataAsJson.roomId;
                    if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                        this.joinedRoom=Room.newRoom(dataAsJson,this);
                    else
                        this.joinedRoom.refreshRoomData(dataAsJson,this);                            
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGamePlayStarted === "function")
                            setTimeout(this.clientEventListener.onGamePlayStarted.bind(this.clientEventListener,this, this.joinedRoom),0);
                }
                break;
            case "#nextTurn#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var turnPlayerAsJson=dataAsJson.turnPlayer;
                    var seatNo=turnPlayerAsJson.seatNo;
                    var userId=turnPlayerAsJson.userId;                        
                    var turnPlayer=this.joinedRoom.getPlayerBySeatNo(seatNo);
                    if(turnPlayer==null){
                        turnPlayer=Player.newPlayer(turnPlayerAsJson,this,this.joinedRoom);
                        this.joinedRoom.set(seatNo, turnPlayer);
                    }
                    else if(turnPlayer.userId!=this.userId){
                        turnPlayer=Player.newPlayer(turnPlayerAsJson,this,this.joinedRoom);
                        this.joinedRoom.set(seatNo, turnPlayer);
                    }
                    else
                        turnPlayer.refreshPlayerData(turnPlayerAsJson,this,this.joinedRoom);
                    this.joinedRoom.setData(dataAsJson.data);
                    this.joinedRoom.curTurnSeatNo=turnPlayer.seatNo;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onNextTurn === "function")
                           setTimeout(this.clientEventListener.onNextTurn.bind(this.clientEventListener,this,turnPlayer,dataAsJson.turnData, this.joinedRoom,this.userId==turnPlayer.userId),0);
                }
                break;
            case "#turnPlayed#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var playerAction=dataAsJson.playerAction;
                    var actionData=dataAsJson.actionData;
                    var turnSeatNo=dataAsJson.turnSeatNo;
                    var roomData=dataAsJson.room;
                    var roomId=roomData.roomId;
                    if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                        this.joinedRoom=Room.newRoom(roomData,this);
                    else
                        this.joinedRoom.refreshRoomData(roomData,this);
                    var turnPlayer=this.joinedRoom.getPlayerBySeatNo(turnSeatNo);
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onTurnPlayed === "function")
                            setTimeout(this.clientEventListener.onTurnPlayed.bind(this.clientEventListener,this, turnPlayer, playerAction, actionData, this.joinedRoom, turnPlayer==null?false:this.userId==turnPlayer.userId),0);
                }
                break;
            case "#invalidAction#":
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var action=dataAsJson.action;
                    var desc=dataAsJson.desc;
                    var errorData=dataAsJson.data;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onInvalidAction === "function")
                            setTimeout(this.clientEventListener.onInvalidAction.bind(this.clientEventListener,this, action, desc, errorData, this.joinedRoom, !this.isCurTurn()),0);
                }
                break;
            case "#gameAction#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var gameAction=dataAsJson.action;
                    var actionData=dataAsJson.data;
                    var roomData=dataAsJson.room;
                    var roomId=roomData.roomId;
                    if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                        this.joinedRoom=Room.newRoom(roomData,this);
                    else
                        this.joinedRoom.refreshRoomData(roomData,this);
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGameAction === "function")
                            setTimeout(this.clientEventListener.onGameAction.bind(this.clientEventListener,this, gameAction, actionData, this.joinedRoom),0);
                }
                break;
            case "#outOfTurnPlayed#":
                if(!this.loggedIn || this.joinedRoom==null){
                    this.lastPlayAction=null;
                    return;
                }
                {
                    var playerAction=dataAsJson.playerAction;
                    var actionData=dataAsJson.actionData;
                    var playerAsJson=dataAsJson.player;
                    var playerSeatNo=dataAsJson.playerSeatNo;
                    var roomData=dataAsJson.room;
                    var outOfTurnPlayer=null;
                    if(playerAsJson!=null){
                        var seatNo=playerAsJson.seatNo;
                        var userId=playerAsJson.userId;                        
                        outOfTurnPlayer=this.joinedRoom.getPlayerBySeatNo(seatNo);
                        if(outOfTurnPlayer==null){
                            outOfTurnPlayer=Player.newPlayer(playerAsJson,this,this.joinedRoom);
                            this.joinedRoom.set(seatNo, outOfTurnPlayer);
                        }
                        else if(outOfTurnPlayer.userId!=userId){
                            outOfTurnPlayer=Player.newPlayer(playerAsJson,this,this.joinedRoom);
                            this.joinedRoom.set(seatNo, outOfTurnPlayer);
                        }
                        else
                            outOfTurnPlayer.refreshPlayerData(playerAsJson,this,this.joinedRoom);

                    }
                    else{
                        var roomId=roomData.roomId;
                        if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                            this.joinedRoom=Room.newRoom(roomData,this);
                        else
                            this.joinedRoom.refreshRoomData(roomData,this);
                        outOfTurnPlayer=this.joinedRoom.getPlayerBySeatNo(playerSeatNo);
                    }
                    if(outOfTurnPlayer!=null && this.userId===outOfTurnPlayer.userId)
                        this.lastPlayAction=null;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onOutOfTurnPlayed === "function")
                            setTimeout(this.clientEventListener.onOutOfTurnPlayed.bind(this.clientEventListener,this, outOfTurnPlayer, playerAction, actionData, this.joinedRoom, outOfTurnPlayer==null?false:this.userId==outOfTurnPlayer.userId),0);
                }
                break;
            case "#endGame#":
                this.lastPlayAction=null;
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var endGameData=dataAsJson.endGameData;
                    var roomData=dataAsJson.room;
                    var roomId=roomData.roomId;
                    if(this.joinedRoom==null || this.joinedRoom.roomId!=roomId)
                        this.joinedRoom=Room.newRoom(roomData,this);
                    else
                        this.joinedRoom.refreshRoomData(roomData,this);
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onGamePlayEnded === "function")
                            setTimeout(this.clientEventListener.onGamePlayEnded.bind(this.clientEventListener,this, this.joinedRoom, endGameData),0);
                }
                break;
            case "#avtarChanged#":
                if(!this.loggedIn)
                    return;
                {
                    var userId=dataAsJson.userId;
                    var avtarId=dataAsJson.avtarId;
                    if(userId==this.userId){
                        this.avtarId=avtarId;
                        if(this.clientEventListener!=null)
                            if(typeof this.clientEventListener.onAvtarChangedOfSelf === "function")
                                setTimeout(this.clientEventListener.onAvtarChangedOfSelf.bind(this.clientEventListener,this, avtarId),0);
                        break;
                    }
                    var room=this.joinedRoom;
                    if(room==null)
                        break;
                    var player=room.getPlayerByUserId(userId);
                    if(player==null)
                        break;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onAvtarChangedOfOtherPlayer === "function")
                            setTimeout(this.clientEventListener.onAvtarChangedOfOtherPlayer.bind(this.clientEventListener,this, player, avtarId, room),0);
                }
                break;
            case "#roomDestroyed#":
                if(!this.loggedIn || this.joinedRoom==null)
                    return;
                {
                    var roomId=dataAsJson.roomId;
                    if(this.joinedRoom.roomId!=roomId)
                        return;
                    if(dataAsJson.gameUserData!=null)
                        this.gameUserData=dataAsJson.gameUserData;
                    var roomDestroyed=this.joinedRoom;
                    this.joinedRoom=null;
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onRoomDestroyed === "function")
                            setTimeout(this.clientEventListener.onRoomDestroyed.bind(this.clientEventListener,this, roomDestroyed),0);
                }
                break;
            case "#notElligibeToPlay#":
                    if(this.clientEventListener!=null)
                        if(typeof this.clientEventListener.onNotEligibleToPlay === "function")
                            setTimeout(this.clientEventListener.onNotEligibleToPlay.bind(this.clientEventListener,this, this.getPlayer(), this.joinedRoom, dataAsJson.reason),0);
                    break;
            case "#invalidAction#":
                if(this.clientEventListener!=null)
                    if(typeof this.clientEventListener.onInvalidAction === "function")
                        setTimeout(this.clientEventListener.onInvalidAction.bind(this.clientEventListener,this, dataAsJson.action, dataAsJson.data),0);
                break;
            default:
                if(command.startsWith("__") && command.endsWith("__")){
                    if(this.clientEventListener!=null){
                        if(typeof this.clientEventListener.onMessageReceivedJson === "function")
                            setTimeout(this.clientEventListener.onMessageReceivedJson.bind(this.clientEventListener,this,command.substring(2, command.length-2), dataAsJson),0);
                    }
                }
                else if(command.startsWith("--") && command.endsWith("__")){
                    if(this.clientEventListener!=null){
                        if(typeof this.clientEventListener.onMessageReceivedString === "function")
                             setTimeout(this.clientEventListener.onMessageReceivedString.bind(this.clientEventListener,this,command.substring(2, command.length-2), data),0);
                    }
                }
        }
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.afterServerMessageProcessed === "function")
                setTimeout(this.clientEventListener.afterServerMessageProcessed.bind(this.clientEventListener,this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command), false,data),0);
    };
    
    this.commandDataReadBytes=function(socket,command,data){
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.beforeServerMessageProcessedBytes === "function")
                setTimeout(this.clientEventListener.beforeServerMessageProcessedBytes.bind(this.clientEventListener,this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),true,data),0);
        if(command.startsWith("--") && command.endsWith("--")){
            if(this.clientEventListener!=null)
                if(typeof this.clientEventListener.onMessageReceivedBytes === "function")
                    setTimeout(this.clientEventListener.onMessageReceivedBytes.bind(this.clientEventListener,this,command.substring(2, command.length()-2), data),0);
        }
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.afterServerMessageProcessedBytes === "function")
                setTimeout(this.clientEventListener.afterServerMessageProcessedBytes.bind(this.clientEventListener,this,ServerMessageType.toEnum(command),ServerMessageType.getCustomCommand(command),true,data),0);
        
    };

    this.setClientEventListener=function (clientEventListener) {
        this.clientEventListener=clientEventListener;
    };
        
    this.getUserId=function() {
        return this.userId;
    };

    this.getSessionId=function() {
        return this.sessionId;
    };

    this.getName=function() {
        return this.name;
    };

    this.getAvtarId=function() {
        return this.avtarId;
    };

    this.getProfilePic=function(){
        return this.profilePic;
    };
    
    this.getEmailId=function() {
        return this.emailId;
    };

    this.getOtherId=function() {
        return this.otherId;
    };
    
    this.getGameName=function(){
        return this.gameName;
    };

    this.getGameUserData=function() {
        return this.gameUserData;
    };

    this.isLoggedIn=function() {
        return this.loggedIn;
    };
    
    this.getLoginType=function() {
        return this.loginType;
    };

    this.getJoinedRoom=function() {
        return this.joinedRoom;
    };
    
    this.hasJoinedRoom=function() {
        return this.joinedRoom!=null;
    };
    
    this.isSpectator=function() {
        var joinedRoom=this.joinedRoom;
        return joinedRoom!=null && !joinedRoom.isSeated(this.userId);
    };
    
    this.isSeated=function() {
        var joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isSeated(this.userId);
    };
    
    this.isGamePlayInProgress=function() {
        var joinedRoom=this.joinedRoom;
        return joinedRoom!=null && joinedRoom.isGamePlayInProgress();        
    };
    
    this.isActivePlayer=function() {
        var player=this.getPlayer();
        return player!=null && player.isActive();
    };
    
    this.getPlayer=function() {
        var joinedRoom=this.joinedRoom;
        if(joinedRoom==null)
            return null;
        return joinedRoom.getPlayerByUserId(this.userId);                    
    };
    
    this.isCurTurn=function() {
        var joinedRoom=this.joinedRoom;
        if(joinedRoom==null || !joinedRoom.gameInProgress)
            return false;
        var player=joinedRoom.getPlayerByUserId(this.userId);
        if(player==null)
            return false;
        return joinedRoom.isCurTurn(player);
    };
    
    this.playAction=function(action,actionData) {
        if(this.joinedRoom==null)
            return;
        if(!this.joinedRoom.gameInProgress)
            throw new Error("Game Not In Progress");
        var jsonObject=new Object();
        jsonObject.action=action;
        jsonObject.data=actionData;
        this.lastPlayAction=action;
        this.lastPlayActionData=actionData;
        this.sendMesg("#playAction#", JSON.stringify(jsonObject));
    };
    
    this.isOwnerOfJoinedRoom=function(room){
        if(room==null)
            return false;
        return room.isOwner(this.userId);
    };

    this.rejoinSession=function() {
        if(this.loggedIn)
            return false;
        if(!this.sessionId)
            this.sessionId=sessionStorage.getItem("gameClient_sessionId");
        if(!this.sessionId)
            return false;
        var sessionId=this.sessionId;
        this.reset();
        var jsonObject=new Object();
        jsonObject.sessionId=sessionId;
        this.sendMesg("#rejoinSession#", JSON.stringify(jsonObject));
        return true;
    };
        
    this.startPoller=function(){
        //start polling timer
        this.poller=new Poller(this);
        setTimeout(this.poller.poll.bind(this.poller), Client.pollingInterval);        
    };
    
    this.stopPoller=function(){
        this.poller=null;
    };
    
    this.socketConnecting=function(socket){
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.onConnecting === "function")
                setTimeout(this.clientEventListener.onConnecting.bind(this.clientEventListener,this),0);        
    };
    
    this.socketConnected=function(socket) {
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.onConnectionSuccess === "function")
                setTimeout(this.clientEventListener.onConnectionSuccess.bind(this.clientEventListener,this),0);
    };

    this.connectionFailed=function (socket,error) {
        System.debug("connection failed");
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.onConnectionFailure === "function")
                setTimeout(this.clientEventListener.onConnectionFailure.bind(this.clientEventListener,this),0);
    };
    
    this.socketDisconnected=function(socket) {
        System.debug("connection disconnected");
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.onConnectionDisconnected === "function")
                setTimeout(this.clientEventListener.onConnectionDisconnected.bind(this.clientEventListener,this),0);
    };

    this.socketClosed=function(socket) {
        if(this.clientEventListener!=null)
            if(typeof this.clientEventListener.onConnectionClosed === "function")
                setTimeout(this.clientEventListener.onConnectionClosed.bind(this.clientEventListener,this),0);
    };
}

Client.pollingInterval=6000;//10000;//

function Poller(client){
    this.client=client;
    this.poll=function() {
        if(this.client.poller!==this || !this.client.isLoggedIn())
            return;
        if(this.client.sessionId===null)
            return;
        var curTime=System.currentTimeMillis();
        if(curTime-this.client.lastSentTime>=Client.pollingInterval){
            this.client.sendMesg("#poll#",this.client.getPollData());
        }
        setTimeout(this.poll.bind(this), Client.pollingInterval);
    };        
}


