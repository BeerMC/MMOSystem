package MMOSystem.events;

/*
 * EconomyAPI: Core of economy system for Nukkit
 * Copyright (C) 2016  onebone <jyc00410@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
