package MMOSystem.commands;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.managers.collect.GardeningManager;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class MMOCommand extends Command{

	private final MMOSystem plugin;

	public MMOCommand(MMOSystem plugin) {
		super("mmo", "MMO command", "/mmo");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!this.plugin.isEnabled()) return false;
		if(!sender.hasPermission("MMOSystem.command.mmo")){
			sender.sendMessage(plugin.getMessage("no-permission"));
			return true;
		}
		if(args.length < 1){
			sender.sendMessage("§7[§c!§7]正确格式: " + this.getUsage());
			return true;
		}
		if(sender instanceof Player){
			String branch = args[0].toLowerCase();
			Manager manager;
			switch(branch){
				case "description":
				case "介绍":
					manager = plugin.getManager(args[1]);
					if(manager != null){
						sender.sendMessage(manager.getDescription());
					}
					break;

				case "exp":
				case "level":
					manager = plugin.getManager(args[1]);
					if(manager != null){
						sender.sendMessage(manager.getExpInfo((Player)sender));
					}
					break;

				case "me":
				case "info":
					manager = plugin.getManager(args[1]);
					if(manager != null){
						sender.sendMessage(manager.getDescription());
						sender.sendMessage(manager.getExpInfo((Player)sender));
						if(manager instanceof GardeningManager){
							((GardeningManager)manager).switchParticle((Player)sender);
						}
					}
					break;
				default:
					return true;
			}
			return true;
		}
		return true;
	}

}