package models;

import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.*;
import akka.dispatch.*;
import static akka.pattern.Patterns.ask;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import java.math.BigDecimal;
import java.util.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * A chat room is an Actor.
 */
public class AuctionRoom extends UntypedActor {
    
    // Default room.
    static ActorRef defaultItem = Akka.system().actorOf(new Props(AuctionRoom.class));
    
    // Item belonging to the Room
    AuctionItem auctionItem;
    
    
    /**
     * Join the default room.
     */
    public static void join(final String username, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
    	System.out.println("join: " + username);
        // Send the Join message to the room
        String result = (String)Await.result(ask(defaultItem,new Join(username, out), 1000), Duration.create(1, SECONDS));
        
        if("OK".equals(result)) {
            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
               public void invoke(JsonNode event) {
                   // Send a Bid message to the room.
            	   defaultItem.tell(new Bid(username, event.get("bid").asText(), event.get("id").asLong()));        
               } 
            });
            
            // When the socket is closed.
            in.onClose(new Callback0() {
               public void invoke() {
                   System.out.println("==== " + username + " disconnected ====");
               }
            });
            
        } else {
            
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            
            // Send the error to the socket.
            out.write(error);
            
        }
        
    }
    
    // Members of this room.
    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();
    
    public void onReceive(Object message) throws Exception {
        
    	System.out.println("onReceive");
    	
        if(message instanceof Join) {
            
            // Received a Join message
            Join join = (Join)message;
            
            // Check if this username is free.
           // if(members.containsKey(join.username)) {
           //     getSender().tell("This username is already used");
           // } else {
                members.put(join.username, join.channel);
                notifyAll("join", join.username, "has entered the room", 1L);
                getSender().tell("OK");
           // }
            
        } else if(message instanceof Bid)  {
            // Received a Bid
            Bid bid = (Bid)message;
            
            notifyAll("bid", bid.username, bid.bid, bid.id);
            System.out.println("onReceive - Bid - har notifierat alla");
            
            // update auctionitem in database TODO
            auctionItem = new AuctionItem();
            auctionItem = AuctionItem.findItem(bid.id); // Fix TODO
            auctionItem.price = new BigDecimal(bid.bid);
            auctionItem.update();
            
        } else if(message instanceof ItemQuery) {
        	
        	ItemQuery itemQuery = (ItemQuery)message;
        	auctionItem = new AuctionItem();
        	//auctionItem = AuctionItem.findItem(ItemQuery.id);
        	
        	
        	
        	
        } else {
            unhandled(message);
        }
        
    }
    
    // Send a Json event to all members
    public void notifyAll(String kind, String user, String text, long id) {
    	System.out.println("notifyAll");
        for(WebSocket.Out<JsonNode> channel: members.values()) {
            
        	ObjectNode event = createMessage(kind, user, text, id);
        	
            // this part redundant? TODO
            /*ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }*/
            channel.write(event);
            System.out.println(event.toString());
            System.out.println("notifyAll - skrivit klart");
        }
    }
    
    public void notifyUser(String kind, String user, String text, long id) {
    	System.out.println("notifyUser");
    	WebSocket.Out<JsonNode> channel = members.get(user);
    	
    	ObjectNode event = createMessage(kind, user, text, id);
       
        channel.write(event);
    	
    }
    
    
    public ObjectNode createMessage(String kind, String user, String text, long id){
    	// write message
    	ObjectNode event = Json.newObject();
        System.out.println("kind " + kind);
        event.put("kind", kind);
        System.out.println("user " + user);
        event.put("user", user);
        System.out.println("text " + text);
        event.put("message", text);
        System.out.println("id " + id);
        event.put("id", id);
        return event;
    	
    }
    
    public ObjectNode itemQueryResponse (AuctionItem auctionItem){
    	ObjectNode event = Json.newObject();
    	event.put("id", auctionItem.id);
    	event.put("name", auctionItem.name);
    	event.put("price", auctionItem.price);
    	event.put("owner", auctionItem.owner);
    	return event;
    }
    
    
    // -- Messages
    
    public static class Join {
        
        final String username;
        final WebSocket.Out<JsonNode> channel;
        
        public Join(String username, WebSocket.Out<JsonNode> channel) {
            this.username = username;
            this.channel = channel;
        }
        
    }
    
    public static class Bid {
        
        final String username;
        final String bid;
        final long id;
        
        public Bid(String username, String bid, Long id) {
            this.username = username;
            this.bid = bid;
            this.id = id;
        }
        
    }
    
    
    public static class ItemQuery {
    	
    	final String username;
    	final long id;
    	
    	public ItemQuery (String username, long id){
    		this.username = username;
    		this.id = id;
    	}
    }
    
}
