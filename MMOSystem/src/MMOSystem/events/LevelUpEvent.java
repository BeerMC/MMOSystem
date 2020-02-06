package MMOSystem.events;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.Player;
import cn.nukkit.Server;

import java.util.UUID;

public class LevelUpEvent extends Event{
	private final static HandlerList handlerList = new HandlerList();
	
	private final String name;

	private final int level;

	private final String job;

	public LevelUpEvent(String name, int level, String job){
		this.name = name;
		this.level = level;
		this.job = job;
	}
	
	public String getPlayerName(){
		return this.name;
	}

	public Player getPlayer(){
		return Server.getInstance().getPlayer(name);
	}

	public int getLevel(){
		return level;
	}

	public String getJob(){
		return job;
	}

	public static HandlerList getHandlers(){
		return handlerList;
	}
}
