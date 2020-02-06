package MMOSystem.utils;

import MMOSystem.MMOSystem;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * @author Winfxk
 */
public class ReloadConfig {
	private static final String V = "1";
	private static final String[] Files = { "experience.yml", "treasures.yml","message.yml", "config.yml"};
	private static final String V_Key = "文件版本";

	/**
	 * 检查资源合法性，是否为适当的版本
	 */
	public static void start() {
		File file;
		MMOSystem plugin = MMOSystem.getPugin();
		for (String name : Files) {
			file = new File(plugin.getDataFolder(), name);
			Config config = new Config(file, Config.YAML);
			if (!config.getString(V_Key).equals(V)){
				file.delete();
				try {
					plugin.getServer().getLogger().info(TextFormat.RED + "更新资源：" + TextFormat.GREEN + name);
					Utils.writeFile(file, plugin.getClass().getResourceAsStream("/resources/" + name));
				} catch (IOException e) {
					plugin.getServer().getLogger().info(TextFormat.RED + "资源：" + TextFormat.GREEN + name + TextFormat.RED
							+ "加载错误！\n" + TextFormat.WHITE + "错误详情：" + e.getMessage());
					plugin.getServer().getPluginManager().disablePlugin(plugin);
				}
			}
		}
	}
}
