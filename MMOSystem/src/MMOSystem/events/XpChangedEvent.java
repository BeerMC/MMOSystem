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
