package controllers;

import models.AuctionItem;

import org.codehaus.jackson.JsonNode;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Jonas"));
    }
    
    /**
     * Handle the auction websocket.
     */
    public static WebSocket<JsonNode> auction(final String username) {
        return new WebSocket<JsonNode>() {
            
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                System.out.println("onReady");
                // Join the chat room.
                try { 
                    AuctionItem.join(username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
  
}
