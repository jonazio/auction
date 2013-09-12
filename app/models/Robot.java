package models;

import play.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import scala.concurrent.duration.*;
import akka.actor.*;
import akka.dispatch.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import static java.util.concurrent.TimeUnit.*;

import java.math.BigDecimal;
import java.util.*;

public class Robot {
	
	String robotName;
	ActorRef auction;
	
    public Robot(ActorRef auctionRoom, String name, int interval) {
        
    	robotName = name;
    	this.auction = auctionRoom;
    	
        // Create a Fake socket out for the robot that log events to the console.
        WebSocket.Out<JsonNode> robotChannel = new WebSocket.Out<JsonNode>() {
            
            public void write(JsonNode frame) {
                Logger.of("robot").info(Json.stringify(frame));
            }
            
            public void close() {}
            
        };
        
        // Join the room
        auctionRoom.tell(new AuctionRoom.Join(robotName, robotChannel), null);
        
        Akka.system().scheduler().schedule(Duration.create(interval, SECONDS), Duration.create(interval, SECONDS), new Runnable() {
        	@Override
        	public void run() {
        		// pick an item randomly that the robot will bid on
        		List<AuctionItem> auctionItems = AuctionItem.findNotOwnedBy(robotName);
        		int itemNo = (int)(Math.random() * auctionItems.size());
        		// the robot will now place a new bid on the item and raise the prize with 1-10 kr
        		BigDecimal newPrice = new BigDecimal((int)(Math.random() * 10 + 1)).add(auctionItems.get(itemNo).price);
        		auction.tell(new AuctionRoom.Bid(robotName, newPrice.toString(), auctionItems.get(itemNo).id), null);
        	}
        }, Akka.system().dispatcher());
        
    }

}
