package MMOSystem.events;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Cancellable;
import cn.nukkit.Player;
import cn.nukkit.Server;

import java.util.UUID;

public class ChangeXpEvent extends Event implements Cancellable{
	private final static HandlerList handlerList = new HandlerList();
	
	private final String name;
	private final String job;
	private int xp;

	public ChangeXpEvent(String name, String job, int xp){
		this.name = name;
		this.job = job;
		this.xp = xp;
	}
	
	public String getPlayerName(){
		return this.name;
	}

	public String getJob(){
		return this.job;
	}

	public int getXp(){
		return this.xp;
	}

	public void setXp(int setting){
		this.xp = setting;
	}

	public Player getPlayer(){
		return Server.getInstance().getPlayer(name);
	}

	public static HandlerList getHandlers(){
		return handlerList;
	}
}
