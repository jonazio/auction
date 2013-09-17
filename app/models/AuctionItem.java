package models;

import java.math.BigDecimal;
import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import com.avaje.ebean.ExpressionList;

@Entity
@Table(name="Auction_Items")
public class AuctionItem extends Model{
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="item_seq")
	@SequenceGenerator(name="item_seq", sequenceName="auction_item_id_seq", allocationSize=21)
	public Long id;
	
	@Required
	public String name;
	
	@Required
	public BigDecimal price;
	
	@Required
	public String owner;
	
	public String bidder;
	
    public static void create (AuctionItem auctionItem) {
    	auctionItem.save();
    }
    
    public static Finder<Long,AuctionItem> find = new Finder(
    	    Long.class, AuctionItem.class
    	  );
    
    public static AuctionItem findItem (Long id){
    	return find.byId(id);
    }
    
    public static void update (Long id, AuctionItem auctionItem){
    	auctionItem.update(id, auctionItem);
    }
    
    public static void delete (Long id) {
    	find.ref(id).delete();
    }
    
    public static List<AuctionItem> all() {
    	return find.all();
    }
    
    public static List<AuctionItem> findByOwner(String owner){
    	List<AuctionItem> items = find.where()
    		    .eq("owner", owner)
    		    .findList();
    	return items;
    }
	
    public static List<AuctionItem> findNotOwnedBy(String owner){
    	List<AuctionItem> items = find.where()
    			.ne("owner", owner)
    			.findList();
    	return items;
    }
    
    public static List<AuctionItem> findAllItems() {
    	List<AuctionItem> items = find.all();
    	return items;
    }
    
}
