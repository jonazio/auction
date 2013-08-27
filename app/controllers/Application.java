package controllers;

import models.AuctionItem;
import models.AuctionRoom;

import org.codehaus.jackson.JsonNode;

import play.*;
import play.mvc.*;
import play.data.Form;
import play.libs.F.*;

import views.html.*;

import models.*;

public class Application extends Controller {
  
	
	static Form<AuctionItem> auctionItemForm = Form.form(AuctionItem.class);
	
	
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
     * Display form for new auction
     */
    public static Result newAuction(String username) {
        if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }  
        return ok(newAuction.render(username, auctionItemForm));
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
     * Display auction items created by the user
     */
    public static Result auctionListUser(String username) {
    	if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }    
        return ok(yourItems.render(username, AuctionItem.findByOwner(username)));
    }
    
    
    /**
     * Create new auction item
     */
    public static Result newAuctionItem(String username) {
    	Form<AuctionItem> filledForm = auctionItemForm.bindFromRequest();
    	if (filledForm.hasErrors()){
    		// add error page redirect
    		return TODO;
    	} else {
    		AuctionItem.create(filledForm.get());
    		return redirect(routes.Application.auctionList(username));
    	}
    }
    
    /**
     * remove auction item
     */
    public static Result removeAuctionItem(String username, Long id) {
    	AuctionItem.delete(id);
    	return ok(yourItems.render(username, AuctionItem.findByOwner(username)));
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
