var springAnimationNoSpring = 'spring(1, 85, 100, 1)';
var springAnimationNoSpringWithVelocity = 'spring(1, 85, 100, 10)';
var springAnimationNoSpringWithHighVelocity = 'spring(1, 85, 100, 20)';
var springAnimationMildSpring = 'spring(1, 80, 15, 10)';

//document.addEventListener('DOMContentLoaded', function () {
//	document.querySelector("#players .middle .player:first-child input").addEventListener("input", function (evt) {
//		evt.target.parentNode.querySelector("span").innerText="$" + evt.target.value;
//	});
//	
//    show(document.getElementById("login"));
//	animateLoginIn();
//	
//	document.querySelector("#login #testButton").addEventListener("click", function (e) {
//		animateLoginOut(animateLobbyIn);
//	});
//	
//	document.querySelector("#lobby #lobbyTitle").addEventListener("click", function (e) {
//		animateLobbyOut("login", animateLoginIn);
//	});
//	
//	document.querySelectorAll("#lobby .flexTableRow").forEach(function (e) {
//		e.addEventListener("click", function (e) {
//			//reloadLobbyTable(upadteLobbyTable);
//			animateLobbyOut("game", animateGameIn.bind(null, function () {
//				show(document.querySelector("#actions"));
//				document.querySelectorAll("#pokerTable .card").forEach(function (card) {
//					card.classList.remove("placeholder");
//				});
//				document.querySelector("#pokerTable .card").classList.add("highlight");
//				document.querySelector("#pokerTable .card:nth-child(3)").classList.add("highlight");
//			}));
//		});
//		
//	});
//	
//	document.querySelectorAll("#players .player").forEach(function (e) {
//		e.addEventListener("click", function(event) {
//			//animateAllSeats(function () {});
//		});
//	})
//	
//	document.querySelector("#topButton").addEventListener("click", function (e) {
//		animateGameOut(animateLobbyIn.bind(null,"game"));
//		return false;
//	});
//	
//	document.querySelector("#playerInfoIcon").addEventListener("click", function (e) {
//		animatePlayerInfoPanel();
//		return false;
//	});
//	
//	document.querySelector("#actions .fold").addEventListener("click", function (e) {
//		animateConnectionError();
//		setTimeout(function () {
//			animateConnectionError("stop");
//		}, 15000);
//	});
//	
//	document.querySelectorAll("#notifications ul button").forEach(function (element) {
//		element.addEventListener("click", animateNotification.bind(null,element.parentNode.parentNode, null));
//	})
//	
//	document.querySelector("#notifications .all .accept").addEventListener("click", function (e) {
//		var element = document.querySelector("#notifications li");
//		element = element.cloneNode(true);
//		animateNotification(element, false);
//	});
//	
//}, false);

function animateLoginIn(direction, callback) {
	anime({
	  targets: '#login > *',
	  translateY: [20, 0],
	  opacity: [0, 1],
	  rotate: function (e, i) {
	  	if (i==0) {
		  		return [25, 0];
		  	}
	  },
	  delay: anime.stagger(70, {from: 'first'}),
	  easing: springAnimationNoSpringWithVelocity,
	  begin: function (anim) {
	  	show(document.getElementById("login"));
	  	show(anim.animatables);
	  	if (direction == "game") {
	  		hide(document.getElementById("game"));
	  		hide(document.getElementById("lobby"));
		}
	  },
	  complete: function () {
	  	if (callback) {
	  	  	callback();
	  	}
	  }
	});
}

function animateLoginOut(callback) {
	var loginElements = document.querySelectorAll("#login > *");
	anime.remove(loginElements);
	anime({
	  targets: loginElements,
	  translateY: [0, 20],
	  opacity: [1, 0],
	  rotate: function (e, i) {
	  	if (i==0) {
	  	  		return [0, 25];
	  	  	}
	  },
	  delay: anime.stagger(40, {from: 'last'}),
	  easing: springAnimationNoSpringWithHighVelocity,
	  complete: function () {
	  }
	});
	anime({
	  targets: '#login',
	  opacity: 0,
	  delay: 400,
	  easing: springAnimationNoSpring,
	  complete: function (anim) {
	  	hide(anim.animatables);
	  }
	});
	setTimeout(function () {
		if (callback) {
			callback();
		}
		
	}, 300);
}

function animateLobbyIn(direction, callback) {
	if (direction == "game") {
		hide(document.querySelector("#actions"));
		var titles = '#lobby h2';
	}else {
		var titles = '#topButton, #playerInfoIcon, #lobby h2';
		hide(document.querySelectorAll("#lobby > *, #game > *"));
	}
	show(document.querySelector("#game"));
	document.querySelectorAll("#pokerTable .card").forEach(function (card) {
		card.classList.remove("highlight");
		card.classList.add("placeholder");
	});
	
	//Poker Table
	anime({
	  targets: '#pokerTable',
	  opacity: function (e) {
	  	return (direction == "game") ? [1, .4] : [0, .4];
	  },
	  duration: 400,
	  easing: 'easeInCubic',
	  begin: function (anim) {
	  	show(anim.animatables);
	  },
	  complete: function () {
	  	animateLobby();
	  }
	});
	
	function animateLobby() {
		//Lobby Background
		anime({
		  targets: '#lobby',
		  opacity: [0, 1],
		  duration: 2000,
		  easing: springAnimationNoSpring,
		  begin: function (anim) {
		  	show(anim.animatables);
		  	show(document.querySelectorAll("#lobby > *"));
		  },
		complete: function(anim) {
			
		  }
		});
		
		//Table Rows
		anime({
		  targets: '#lobby .flexTableRow',
		  translateX: [-20, 0],
		  opacity: [0, 1],
		  delay: anime.stagger(100),
		  easing: springAnimationNoSpring,
		  complete: function (anim) {
		  	if (callback) {
			  		callback();
			  	}
		  }
		});
			
		anime({
		  targets: titles,
		  translateX: [-20, 0],
		  opacity: [0, 1],
		  delay: anime.stagger(100, {from: 'last'}),
		  easing: springAnimationNoSpring,
		  begin: function (anim) {
		  	show(anim.animatables);
		  }
		});
	}
		
}

function reloadLobbyTable(upadteTable) {
	//Hoshi Has to complete this
	//Animate row out complete should be called only for the last elem. And your table should be updated then
	elements = document.querySelectorAll("#lobby .flexTableRow");
	var time = 100;
        if(elements){
            for(var ctr=0;ctr<elements.length;ctr++){
                setTimeout(animateRowOut.bind(null, elements[ctr],ctr===elements.length-1), time);
                time += 200;                
            }
        }
		
	function animateRowOut(element,doComplete) {
		var animation = anime({
		  targets: element,
		  translateX: [0, 20],
		  opacity: [1, 0],
		  duration: 300,
		  easing: springAnimationNoSpringWithHighVelocity,
		  complete: function (anim) {
              if(!doComplete)
                  return;
			var table = document.querySelector("#lobby .flexTable");
			
			hide(table);
            upadteTable();
			
		  	elements = document.querySelectorAll("#lobby .flexTableRow");
		  	var time = 100;
		  	elements.forEach(function (element) {
		  		element.style.opacity = 0;
		  		setTimeout(animateRowIn.bind(null, element), time);
		  		time += 200;
		  	});
		  	show(table);
		  	
		  }
		});
	}
	
	function animateRowIn(element) {
		var animation = anime({
		  targets: element,
		  translateX: [-20, 0],
		  opacity: [0, 1],
		  easing: springAnimationNoSpringWithHighVelocity,
		});
	}
}

//For destination use either "login" or "game"
function animateLobbyOut(destination, callback) {
	anime.remove(document.querySelectorAll("#lobby .flexTableRow, #topButton, #playerInfoIcon, #lobby h2"));
	if (destination == "login") {
		var titles = document.querySelectorAll("#topButton, #playerInfoIcon, #lobby h2");
		var lobbyAnimationCurve = springAnimationNoSpring;
	}else {
		var titles = document.querySelectorAll("#lobby h2");
		var lobbyAnimationCurve = 'easeInCubic';
	}
	//Table Rows
	anime({
	  targets: '#lobby .flexTableRow',
	  translateX: [0, 20],
	  opacity: [1, 0],
	  delay: anime.stagger(70),
	  easing: springAnimationNoSpring,
	  complete: function (anim) {
	  }
	});
	
	anime({
	  targets: titles,
	  translateX: [0, 20],
	  opacity: [1, 0],
	  delay: anime.stagger(100, {from: 'last'}),
	  easing: springAnimationNoSpring,
	  begin: function (anim) {
	  	
	  },
	  complete: function (anim) {
	  	hide(anim.animatables);
	  	finishAnimation();
	  }
	});
	
	function finishAnimation(){
		//Fade Lobby
			anime({
			  targets: '#lobby',
			  opacity: [1, 0],
			  duration: 1000,
			  easing: lobbyAnimationCurve,
			  complete: function (anim) {
			  	hide(anim.animatables);
			  }
			});
			
			if (destination == "login") {
			  	//Fade Out Table
			  	anime({
					  targets: '#game',
					  opacity: [1, 0],
					  duration: 800,
					  easing: springAnimationNoSpring,
			  		  begin: function () {
			  		  },
					  complete: function () {
					  	hide(document.getElementById("game"));
					  }
			  	});
			  	callback();
			}else if (destination == "game") {
				//Show Table
				anime({
					  targets: '#pokerTable',
					  opacity: [0.4, 1],
					  duration: 800,
					  easing: 'easeInCubic',
					  begin: function () {
					  	show(document.getElementById("game"));
					  	hide(document.querySelector("#game #pot span"));
					  	document.getElementById("pokerTable").classList.remove("dim");
					  	document.getElementById("pokerTable").classList.remove("hidden");
					  	show(document.querySelectorAll("#topButton, #playerInfoIcon"));
					  	hide(document.querySelector("#players"));
					  	hide(document.getElementById("actions"));
					  },
					  complete: function () {
					  	if (callback) {
						  		callback();
						  	}
					  	adjustForWideAspectRatio();
					  }
				});
				document.querySelectorAll("#pokerTable .card").forEach(function (card) {
					card.classList.remove("highlight");
					card.classList.add("placeholder");
				});
		}
	}
}
function showGameAfterRejoin(callback) {
	animateGameIn(callback, true);
}

function animateGameIn(callback, dontAnimate) {
	if (dontAnimate) {
		show(document.getElementById("game"));
		show(document.getElementById("pokerTable"));
		show(document.getElementById("players"));
        show(document.getElementById("topButton"));
        show(document.getElementById("playerInfoIcon"));
		hide(document.getElementById("login"));
		if (callback) {
			callback();
		}
        return false;
	}
	
	var chips = document.querySelectorAll("#players .player .chip");
	chips.forEach(function (chip) {
		chip.style.opacity = 0;
	});
	var whichOneIsYou = 0;
	var players = document.querySelectorAll("#players .player:not(.hidden)");
	players.forEach(function(e, i){
		if(e.classList.contains("active")){
			whichOneIsYou = i;
		}
	});
	anime({
		  targets: players,
		  scale: function (e) {
		  	if (e.classList.contains("you")) {
			  		return [0, 1.3];
			  	}
		  	return [0, 1];
		  },
		  opacity: [0, 1],
		  delay: anime.stagger(100, {from: whichOneIsYou, start: 400}),
		  easing: springAnimationMildSpring,
		  begin: function (anim) {
		  	show(document.getElementById("players"));
		  	show(anim.animatables);
		  	anim.animatables.forEach(function (element) {
		  		hide(element.target.querySelector(".timer"));
		  	});
		  },
		  complete: function (anim) {
		  	if (callback) {
			  	callback();
			}
			anim.animatables.forEach(function (element) {
				element = element.target;
				element.removeAttribute("style");
				var chip = element.querySelector(".chip");
				if (chip) {
					chip.style.opacity = 1;
				}
				show(element.querySelector(".timer"));
			});
			show(document.querySelector("#game #pot span"));
		  }
	});
}

function animateGameOut(callback) {
	var players = document.querySelectorAll("#players .player:not(.hidden)");
	anime.remove(players);
	var chips = document.querySelectorAll("#players .player .chip");
	chips.forEach(function (chip) {
		chip.style.opacity = 0;
	});
	
	var whichOneIsYou = 0;
	players.forEach(function(e, i){
		if(e.classList.contains("you")){
			whichOneIsYou = i;
		}
	});
	document.querySelectorAll("#pokerTable .card").forEach(function (card) {
		card.classList.remove("highlight");
		card.classList.add("placeholder");
	});
	anime({
		  targets: players,
		  scale: function (e) {
		  	return [getScaleOfElement(e), 0];
		  },
		  opacity: 0,
		  delay: anime.stagger(70, {from: whichOneIsYou}),
		  easing: springAnimationMildSpring,
		  begin: function (anim) {
		  	show(document.getElementById("game"));
		  	show(document.getElementById("players"));
		  	hide(document.querySelector("#game #pot span"));
		  	hide(document.querySelector("#game .gameEnded"));
		  	hide(document.querySelector("#game .shadow"));
		  	show(anim.animatables);
		  },
		  complete: function () {
		  	if (callback) {
			  		callback();
			  	}
		  }
	});
	setTimeout(function () {
		anime.remove(players);
		if (callback) {
		  		callback();
		  	}
	}, 1400)
}

function animateSeatOut(whichSeat, callback) {
	anime.remove(whichSeat);
	var scaleRange = [1, 0];
	if (whichSeat.classList.contains("you")) {
		scaleRange = [1.3, 0];
	}
	anime({
			  targets: whichSeat,
			  scale: 0,
			  opacity: 0,
			  duration: 500,
			  easing: 'easeOutQuart',
			  begin: function (anim) {
			  	show(document.getElementById("players"));
			  	//show(this.animatables);
			  },
			  complete: function (anim) {
			  	if (callback) {
				  	callback();
				}
			  }
		});
}

function animateSeatIn(whichSeat, callback) {
	anime.remove(whichSeat);
	var scaleRange = [0, 1];
	var targetOpacity = [0, 1];
	if (whichSeat.classList.contains("dim")) {
		targetOpacity = [0, .6];
	}
	if (whichSeat.classList.contains("you")) {
		scaleRange = [0, 1.3];
		targetOpacity = [0, 1];
	}
		anime({
				  targets: whichSeat,
				  scale: scaleRange,
				  opacity: targetOpacity,
				  easing: springAnimationMildSpring,
				  complete: function (anim) {
				  	anim.animatables[0].target.removeAttribute("style");
				  	if (callback) {
				  	  	callback();
				  	}
				  }
			});
}

function animateTimer(player, duration, startingPoint, callback) {
	if (player.classList.contains("you")) {
		player = document.querySelector("#actions");
	}
	var timerBar = player.querySelector(".bar");
	duration = (duration) ? duration : 15000;
	startingPoint = (startingPoint) ? startingPoint : "100%";
	anime.remove(timerBar);
	
	anime({
	  targets: timerBar,
	  width: [startingPoint, "1%"],
	  duration: duration,
	  cycles: 130,
	  easing: "linear",
	  begin: function (anim) {
	  	show(anim.animatables[0].target.parentNode);
	  },
	  complete : function (anim) {
	  	hide(anim.animatables[0].target.parentNode);
	  	if (callback) {
		  	callback();
		}
	  }
	});
}

function stopTimerAnimation(player) {
	if (player.classList.contains("you")) {
		player = document.querySelector("#actions");
	}
	var timerBar = player.querySelector(".bar");
	anime.remove(timerBar);
	hide(timerBar.parentNode);
}

function animateWinners(startOrStop, callback, element) {
	var gameEnded = document.querySelectorAll("#pokerTable .gameEnded, #pokerTable .shadow");
	if (element && startOrStop !== "start") {
		anime.remove(element);
		element.removeAttribute("style");
		show(document.getElementById("pot"));
		hide(gameEnded);
		element.classList.remove("winner");
		if (callback) {
				callback();
		}
		return false;
	}
	var winners = document.querySelectorAll("#players .player.winner");
	if (winners.length < 0) {
		if (callback) {
				callback();
		}
		return false;
	}
	winners.forEach(function (winner) {
		anime.remove(winner);
		var start = (getScaleOfElement(winner)) ? getScaleOfElement(winner) : 1;
		hide(winner.querySelector(".chip"));
		if (startOrStop && startOrStop !== "start") {
			
			anime({
			  targets: winner,
			  scale: function (e) {
			  	if (e.classList.contains("you")) {
				  		return 1.3;
				  	}else {
					  		return 1;
					  	}
			  },
			  easing : 'easeInOutQuad',
			  duration: 800,
			  complete: function () {
			  	winner.removeAttribute("style");
			  	show(winner.querySelector(".chip"));
			  	//Hide Winning Hand
			  	show(document.getElementById("pot"));
			  	hide(gameEnded);
			  	
			  	if (callback) {
				  	callback();
				}
			  },
			  begin: function () {
			  	winner.classList.remove("winner");
			  }
			});
			
		}else if (!startOrStop || startOrStop == "start") {
			if (!document.querySelector("#pot span").innerText.includes("$")) {
				//Show Winning Hand
				anime({
					targets: gameEnded,
					width: function (e) {
						e.removeAttribute("style");
						return ["0px", window.getComputedStyle(e).width];
					},
					duration: 600,
					begin: function () {
						gameEnded[0].querySelector("h2").innerText = document.querySelector("#pot span").innerText.split("_").join(" ");
						hide(document.getElementById("pot"));
						show(gameEnded);
						if(callback){
							callback();
						}
					}
				});
			}
			anime({
			  targets: winner,
			  scale: [start, "+=.08"],
			  easing : 'easeInOutQuad',
			  duration: 1000,
			  loop: true,
			  direction: "alternate"
			});
		}
	});
}

function animateConnectionError(message, startOrStop, pulse, callback) {
	var message = (message) ? message : "Reconnecting...";
	var error = document.getElementById("error");
	error.querySelector("h2").innerText = message;
	anime.remove(error);
	if (startOrStop && startOrStop !== "start") {
		//Stop Code
		var startPoint = getScaleOfElement(error);
		anime({
			targets: error,
			scale: [startPoint, 0],
			duration: 600,
			easing: 'easeInCubic',
			complete: function (anim) {
				hide(error);
				if (callback) {
					callback();
				}
			}
		});
		
	}else if (!startOrStop || startOrStop == "start") {
		//Start Code
		anime({
			targets: error,
			scale: [0, 1],
			duration: 800,
			begin: function (anim) {
				show(error);
			},
			complete: function (anim) {
				if (pulse) {
					anime({
					  targets: error,
					  scale: [1, "+=.08"],
					  easing : 'easeInOutQuad',
					  duration: 1000,
					  loop: true,
					  direction: "alternate"
					});
				}
				if (callback) {
					callback();
				}
			}
		});
	}
}

function animatePlayerInfoPanel(callback) {
	var icon = document.getElementById("playerInfoIcon");
	var panel = document.getElementById("playerInfoPanel");
	var panelOffset = -(panel.offsetWidth+ 20);
	if (panel.classList.contains("hidden")) {
		hide(icon);
		show(panel);
		anime({
			targets: panel,
			left: [panelOffset, 0],
			duration: 800,
			easing : springAnimationNoSpringWithVelocity,
			complete: function (anim) {
				if (callback) {
					callback();
				}
			}
		});
		
	}else {
		show(icon);
		anime({
			targets: panel,
			left: [0, panelOffset],
			duration: 800,
			easing : springAnimationNoSpringWithVelocity,
			complete: function (anim) {
				hide(panel);
				if (callback) {
					callback();
				}
			}
		});
		
	}
}
function animateNotification(element, callback) {
	if (isVisible(element) && element.parentNode) {
		anime({
			targets: element,
			scale: [1, 0],
			height: [element.offsetHeight, 0],
			duration: 300,
			easing : 'easeOutQuad',
			complete: function (anim) {
				element.parentNode.removeChild(element);
				if (callback) {
					callback();
				}
			}
		});
	}else {
		document.querySelector("#notifications ul").prepend(element);
		var elemHeight;
		anime({
			targets: element,
			scale: [0, 1],
			height: [0, elemHeight],
			duration: 300,
			easing : 'easeOutQuad',
			begin: function () {
				show(element);
				elemHeight = element.offsetHeight;
			},
			complete: function (anim) {
				if (callback) {
					callback();
				}
			}
		});
	}
}

//Helper Functions
//-----------------
function getScaleOfElement(element) {
	return element.getBoundingClientRect().width / element.offsetWidth;
}

function isFireFox(){
    return navigator.userAgent.indexOf("Firefox") >= 0;
}

function isChrome(){
    return navigator.userAgent.indexOf("Chrom") >= 0;
}

function isSafari(){
    return navigator.userAgent.indexOf("Safari") >= 0;
}

function adjustForWideAspectRatio() {
        var multiplier=1.0;
        if(isFireFox())
            multiplier=1.015;
        else if(isChrome())
            multiplier=1.01;
	var playersDiv = document.querySelector("#players");
	var gameDiv = document.querySelector("#game");
	var aspectRatioOfPlayers;
        if(gameDiv.classList.contains("hidden")){
            gameDiv.style.visibility = "hidden";
            show(gameDiv);            
            if (playersDiv.classList.contains("hidden")) {
                playersDiv.style.visibility = "hidden";
                show(playersDiv);
                aspectRatioOfPlayers = playersDiv.clientWidth / (playersDiv.clientHeight*multiplier);
                hide(playersDiv);
                playersDiv.style.visibility = "visible";
            }else {
                    aspectRatioOfPlayers = playersDiv.clientWidth / (playersDiv.clientHeight*multiplier);
            }
            hide(gameDiv);
            gameDiv.style.visibility = "visible";            
        }
        else{
            if (playersDiv.classList.contains("hidden")) {
                playersDiv.style.visibility = "hidden";
                show(playersDiv);
                aspectRatioOfPlayers = playersDiv.clientWidth / (playersDiv.clientHeight*multiplier);
                hide(playersDiv);
                playersDiv.style.visibility = "visible";
            }else {
                    aspectRatioOfPlayers = playersDiv.clientWidth / (playersDiv.clientHeight*multiplier);
            }            
        }
		
	var aspectRatioOfWindow;
	if (window.outerHeight < window.outerWidth) {
		aspectRatioOfWindow = window.outerWidth/window.outerHeight;
	}else{
		aspectRatioOfWindow = window.outerHeight/window.outerWidth;
	}
        console.log("window ratio=" + aspectRatioOfWindow);
	
	if (aspectRatioOfWindow > aspectRatioOfPlayers) {
		document.getElementById("main").classList.add("tooWide");
	}else {
		if (document.getElementById("main").classList.contains("tooWide")) {
			document.getElementById("main").classList.remove("tooWide");
		}
	}	
}

function stopAllAnimations() {
	anime.running.forEach(function (e) {
		e.animatables.forEach(function (element) {
			anime.remove(element.target);
		});
	});
}
function getAllAnimatingElements() {
	var elements = [];
	anime.running.forEach(function (e) {
		e.animatables.forEach(function (element) {
			elements.push(element.target);
		});
	});
	return elements;
}

function show(elements){
	if (elements == undefined) {
		return false;
	}
	if (elements[0] == undefined) {
		if (elements.style) {
			elements.style.opacity = 1;
		}
		if (elements.classList) {
			if (elements.classList.contains("hidden")) {
				elements.classList.remove("hidden");
			}
		}	
	}else {
		elements.forEach(function (element) {
			if (element.target !== undefined) {
				element.target.style.opacity = 1;
				element.target.classList.remove("hidden");
			}else {
				element.style.opacity = 1;
				element.classList.remove("hidden");
			}
		});
	}
}

function hide(elements){
	if (elements == undefined) {
		return false;
	}
	if (elements[0] == undefined) {
		elements.classList.add("hidden");
	}else {
		elements.forEach(function (element) {
			if (element.target !== undefined) {
				element.target.classList.add("hidden");
			}else {
				element.classList.add("hidden");
			}
		});
	}
}

function toggle(elements) {
	if (elements == undefined) {
		return false;
	}
	if (elements[0] == undefined) {
		if (elements.style.display == "hidden" || elements.classList.contains("hidden")) {
			show(elements);
		}else {
			hide(elements);
		}
	}else {
		elements.forEach(function (element) {
			if (elements.style.display == "hidden" || elements.classList.contains("hidden")) {
				show(elements);
			}else {
				hide(elements);
			}
		});
	}
}
function isVisible (ele) {
    var style = window.getComputedStyle(ele);
    return  style.width !== "0" &&
    style.height !== "0" &&
    style.opacity !== "0" &&
    style.display!=='none' &&
    style.visibility!== 'hidden';
}
