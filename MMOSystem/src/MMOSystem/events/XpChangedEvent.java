package MMOSystem.events;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.Player;
import cn.nukkit.Server;

import java.util.UUID;

public class XpChangedEvent extends Event{
	private final static HandlerList handlerList = new HandlerList();
	
	private final String name;

	public XpChangedEvent(String name){
		this.name = name;
	}
	
	public String getPlayerName(){
		return this.name;
	}

	public Player getPlayer(){
		return Server.getInstance().getPlayer(name);
	}

	public static HandlerList getHandlers(){
		return handlerList;
	}
}
