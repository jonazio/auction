package models;

import java.math.BigDecimal;
import java.util.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
public class AuctionItem extends Model{
	
	@Id
	public Long id;
	
	@Required
	public String name;
	
	public BigDecimal price;
	
    public static void create (AuctionItem auctionItem) {
    	auctionItem.save();
    }
    
    public static Finder<Long,AuctionItem> find = new Finder(
    	    Long.class, AuctionItem.class
    	  );
    
    public static AuctionItem findItem (Long id){
    	return find.byId(id);
    }
    
    public void update (Long id, AuctionItem auctionItem){
    	auctionItem.update(id, auctionItem);
    }
	
}
