package controllers;

import play.GlobalSettings;
import models.AuctionRoom;
import play.Application;
import play.Logger;

public class Global extends GlobalSettings{
	
	@Override
	public void onStart(Application app){
		Logger.info("====== Application has started =======");
		AuctionRoom.jmsConsumer.consume();
	}

}
