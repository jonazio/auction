$(document).ready(function() {
            var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	        var auctionSocket = new WS("ws://localhost:9000/auction/room/auction?username=Jonas")
            
            var sendMessage = function() {
            	auctionSocket.send(JSON.stringify(
                    {kind: "bid", bid: $("#bid").val(), id: @auctionItem.id}
                ))
                $("#bid").val('')
            }
            
            var itemQuery = function() {
            	auctionSocket.send(JSON.stringify(
            				{kind: "deleteitem", id: @auctionItem.id}
            			))
            	$("#bid").val('')
            };
            
            var receiveEvent = function(event) {
                var data = JSON.parse(event.data)
                
                // Handle errors
                if(data.error) {
                	auctionSocket.close()
                    $("#onError span").text(data.error)
                    $("#onError").show()
                    return
                } 
                
                if (data.kind == 'bid') {
                	if (data.id == @auctionItem.id) {
										 $("#currentBid").val(data.message)
											var el = $('<div class="message"><p></p></div>')
	                    $("span", el).text(data.user)
	                    var today = new Date()
											var h = today.getHours()
											var m = today.getMinutes()
											var s = today.getSeconds()
											m=checkTime(m);
											s=checkTime(s);
	                    $("p", el).text(h+":"+m+":"+s+": "+ data.user + " budade " + data.message + " kr.")
	                    $(el).addClass(data.kind)
	                    if(data.user == '@username') $(el).addClass('me')
	                    $('#auctionhistory').append(el)
                	}
                }
            }
            
            var handleReturnKey = function(e) {
                if(e.charCode == 13 || e.keyCode == 13) {
                    e.preventDefault()
                    sendMessage()
                } 
            }
            
            $("#bid").keypress(handleReturnKey)  
            
            auctionSocket.onmessage = receiveEvent
            
            $("#bidButton").click(function() {sendMessage()})
            
            function checkTime(i)
			{
			if (i<10)
			  {
			  i="0" + i;
			  }
			return i;
			}
                        
        })