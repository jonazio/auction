package controllers;

import models.AuctionItem;
import models.AuctionRoom;

import org.codehaus.jackson.JsonNode;

import play.*;
import play.mvc.*;
import play.libs.F.*;

import views.html.*;

import models.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render());
    }

    /**
     * Display the auction room. Remove when new solution is working TODO
     */
    public static Result auctionRoom(String username) {
        if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }     
        // value is static right now, obviously going to change it when the site grows TODO
        return ok(auctionRoom.render(username, AuctionItem.findItem(1L)));
    }
    
    /**
     * Display the auction room. 
     */
    public static Result auctionRoom(String username, Long id) {
        if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }     
        return ok(auctionRoom.render(username, AuctionItem.findItem(id)));
    }
    

     
    /**
     * Display the list of items TODO
     */
    public static Result auctionList(String username){
        if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }    
        return ok(auctionList.render(username, AuctionItem.all()));
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
                    AuctionRoom.join(username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
  
}
