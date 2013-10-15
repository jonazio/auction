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
    
    // Test
    //static JMSConsumer jmsConsumer = new JMSConsumer();
    
    static {
        //new Robot(defaultItem, "Sigvard", 3);
        //new Robot(defaultItem, "Gösta", 2);
        //new Robot(defaultItem, "Nils-Erik", 4);
    }
    
    
    /**
     * Join the default room.
     */
    public static void join(final String username, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
    	System.out.println("join: " + username);
    	
    	//jmsConsumer.consume();
        // Send the Join message to the room
        String result = (String)Await.result(ask(defaultItem,new Join(username, out), 1000), Duration.create(1, SECONDS));
        
        if("OK".equals(result)) {
            // For each event received on the socket,
        	
            in.onMessage(new Callback<JsonNode>() {
            	           	
               public void invoke(JsonNode event) {
            	   	System.out.println("invoke");
            	   	System.out.println(event.get("kind").asText());
               	// determine what type of message we've received
               	if (event.get("kind").asText().equals("bid")) {
               		// Send a Bid message to the room.
            		defaultItem.tell(new Bid(username, event.get("price").asText(), event.get("id").asLong()), null);
            	}
               	if (event.get("kind").asText().equals("getitemrequest")) {
               		// respond with an itemqueryresponse
            		defaultItem.tell(new GetItem(username, event.get("id").asLong()), null);
               	}
               	if (event.get("kind").asText().equals("deleteitemrequest")) {
               		defaultItem.tell(new DeleteItem(username, event.get("id").asLong()), null);
               	}
               	if (event.get("kind").asText().equals("getallitemsrequest")) {
               		defaultItem.tell(new GetAllItems(username), null);
            	}
               	if (event.get("kind").asText().equals("newauctionrequest")) {
               		defaultItem.tell(new NewItem(username, event.get("name").asText(), new BigDecimal(event.get("price").asText())), null);
            	}
                    
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
            	System.out.println("onreceive");
        if(message instanceof Join) {
            
            // Received a Join message
            Join join = (Join)message;
            
            // Check if this username is free.
           // if(members.containsKey(join.username)) {
           //     getSender().tell("This username is already used");
           // } else {
                members.put(join.username, join.channel);
                notifyAll(Join.JoinMessage(join.username));
                getSender().tell("OK", getSender());
           // }
            
        } else if(message instanceof Bid)  {
            // Received a Bid
            Bid bid = (Bid)message;
            
            notifyAll(Bid.BidMessage(bid.username, bid.bid, bid.id));
            
            // update auctionitem in database TODO
            auctionItem = new AuctionItem();
            auctionItem = AuctionItem.findItem(bid.id); // Fix TODO
            auctionItem.price = new BigDecimal(bid.bid);
            auctionItem.bidder = bid.username;
            auctionItem.update();
            
        } else if(message instanceof GetItem) {
        	
        	GetItem getItem = (GetItem)message;
        	
        	// find item
        	auctionItem = new AuctionItem();
        	auctionItem = AuctionItem.findItem(getItem.id);
        	
        	// send response to user TODO
        	notifyUser(getItem.username, GetItem.getItemResponse(auctionItem));
        } else if (message instanceof GetAllItems) {
        	
        	GetAllItems getAllItems = (GetAllItems)message;
        	
        	// find all items
        	List<AuctionItem> auctionItems = AuctionItem.findAllItems();
        	
        	notifyUser(getAllItems.username, GetAllItems.getAllItemsResponse(auctionItems));
        	
        } else if(message instanceof DeleteItem) {
        	
        	DeleteItem deleteItem = (DeleteItem)message;
        	
        	// delete item
        	AuctionItem.delete(deleteItem.id);
        	
        	notifyUser(deleteItem.username, DeleteItem.DeleteItemResponse(deleteItem.id));
 	
        } else if(message instanceof NewItem){
        	
        	NewItem newItem = (NewItem)message;
        	
        	AuctionItem.create(newItem.auctionItem);
        	
        	notifyAll(NewItem.NewItemResponse(newItem.auctionItem));
        	
        }
        else {
            unhandled(message);
            System.out.println("unhandled message");
        }
        
    } 
    
    // Send a Json event to all members, remove this function later on TODO
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
        }
    }
    
    // Send a Json event to all members
    public void notifyAll(ObjectNode event) {
    	System.out.println("notifyAll");
        for(WebSocket.Out<JsonNode> channel: members.values()) {
            
        	
            // this part redundant? TODO
            /*ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }*/
            channel.write(event);
            System.out.println(event.toString());
        }
    }
    
    public void notifyUser(String user, ObjectNode event) {
    	System.out.println("notifyUser " + user);
    	WebSocket.Out<JsonNode> channel = members.get(user);   
    	System.out.println(event.toString());
        channel.write(event);  	
    }
    
    
    public ObjectNode createMessage(String kind, String user, String text, long id){
    	// write message
    	ObjectNode event = Json.newObject();
        event.put("kind", kind);
        event.put("user", user);
        event.put("message", text);
        event.put("id", id);
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
        
        public static ObjectNode JoinMessage(String username){
        	ObjectNode event = Json.newObject();
            event.put("kind", "join");
            event.put("user", username);
            event.put("message", "has entered the room");
            return event;        	
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
        
        public static ObjectNode BidMessage(String username, String bid, Long id){
        	ObjectNode event = Json.newObject();
            event.put("kind", "bid");
            event.put("user", username);
            event.put("price", bid);
            event.put("id", id);
            return event;
        }
        
    }
    
    public static class GetItem {
    	
    	final String username;
    	final long id;
    	
    	public GetItem (String username, long id){
    		this.username = username;
    		this.id = id;
    	}
    	
    	public static ObjectNode getItemResponse(AuctionItem auctionItem){
        	ObjectNode event = Json.newObject();
        	event.put("kind", "getitemresponse");
        	event.put("id", auctionItem.id);
        	event.put("name", auctionItem.name);
        	event.put("price", auctionItem.price);
        	event.put("owner", auctionItem.owner);
        	event.put("bidder", auctionItem.bidder);
        	return event;
    	}
    	
    	public static ObjectNode getItemData (AuctionItem auctionItem){
    		ObjectNode event = Json.newObject();
        	event.put("id", auctionItem.id);
        	event.put("name", auctionItem.name);
        	event.put("price", auctionItem.price);
        	event.put("owner", auctionItem.owner);
        	event.put("bidder", auctionItem.bidder);
        	return event;
    	}
    }
    
    public static class GetAllItems {
    	
    	final String username;
    	
    	public GetAllItems (String username){
    		this.username = username;
    	}
    	
    	public static ObjectNode getAllItemsResponse(List<AuctionItem> auctionItems) {
    		ObjectNode event = Json.newObject();
    		event.put("kind", "getallitemsresponse");
    		//System.out.println(auctionItems.size());
    		final JsonNodeFactory factory = JsonNodeFactory.instance;
    		ArrayNode list = new ArrayNode(factory);
    		for (int a = 0; a < auctionItems.size(); a++){
    			//event.put("item " + a, GetItem.getItemData(auctionItems.get(a)));
    			list.add(GetItem.getItemData(auctionItems.get(a)));
    		}
    		event.put("items", (JsonNode)list);
    		return event;
    	}	
    }
    
    public static class DeleteItem {
    	
    	final String username;
    	final long id;
    	
    	public DeleteItem (String username, long id){
    		this.username = username;
    		this.id = id;
    	}
    	
    	public static ObjectNode DeleteItemResponse(long id){
    		ObjectNode event = Json.newObject();
        	event.put("kind", "deleteitemresponse");
        	event.put("id", id);
        	return event;	
    	}
    	
    }
    
    
    public static class NewItem {
    	
    	final AuctionItem auctionItem;
    	
    	public NewItem(String owner, String name, BigDecimal price){
    		auctionItem = new AuctionItem();
    		auctionItem.owner = owner;
    		auctionItem.name = name;
    		auctionItem.price = price;
    	}
    	
    	public static ObjectNode NewItemResponse(AuctionItem auctionItem){
    		ObjectNode event = Json.newObject();
    		event.put("kind", "newitemresponse");
    		event.put("id", auctionItem.id);
    		event.put("name", auctionItem.name);
    		event.put("price", auctionItem.price);
    		event.put("owner", auctionItem.owner);
    		return event;
    	}
    	
    }
}
