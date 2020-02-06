package MMOSystem.utils;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import MMOSystem.MMOSystem;

/**
 * @author Winfxk
 */
@SuppressWarnings("unchecked")
public class Message {
	private MMOSystem plugin;
	/**
	 * 插件消息文本文件
	 */
	private Config msgconfig;
	private String[] Global_Key;
	private String[] Global_Data;
	public static String[] ReloadKey = { "\n" };
	public static String[] ReloadData = { "{n}" };

	public Message(MMOSystem plugin) {
		this.plugin = plugin;
		this.msgconfig = new Config(this.plugin.getDataFolder() + "/message.yml", Config.YAML);
		this.update();
	}

	/**
	 * 获取孙级自定义消息
	 * 
	 * @param Main    父级Key
	 * @param Son     子级Key
	 * @param Surname 孙级Key
	 * @return
	 */
	public String getSurname(String Main, String Son, String Surname) {
		return getSurname(Main, Son, Surname, new String[] {}, new Object[] {});
	}

	/**
	 * 获取孙级自定义消息
	 * 
	 * @param Main    父级Key
	 * @param Son     子级Key
	 * @param Surname 孙级Key
	 * @param MsgKey  要替换的数据键
	 * @param MsgData 要替换的数据值
	 * @return
	 */
	public String getSurname(String Main, String Son, String Surname, String[] MsgKey, Object[] MsgData) {
		HashMap<String, Object> map = new HashMap<>();
		if (msgconfig.get(Main) instanceof Map)
			map = (HashMap<String, Object>) msgconfig.get(Main);
		HashMap<Object, String> SurnameMap = new HashMap<>();
		if (map.get(Son) instanceof Map)
			SurnameMap = (HashMap<Object, String>) map.get(Son);
		return getText(SurnameMap.get(Surname), MsgKey, MsgData);
	}

	/**
	 * 获取语言消息
	 * 
	 * @param Key 语言消息存储在配置文件中的索引键
	 * @return 语言消息
	 */
	public String getMessage(String Key) {
		return this.getMessage(Key, new String[] {}, new Object[] {});
	}

	/**
	 * 更新数据
	 */
	public void update() {
		Global_Key = new String[] { "{moneyname}", "{coinname}","{n}",  "{servername}", "{time}", "{date}",
				"{Rand_Color}" };
		Global_Data = new String[] { "金币", "电器", "\n", plugin.getServer().getMotd(), Tool.getTime(), Tool.getDate()};
	}

	/**
	 * 获取带子项的消息
	 * 
	 * @param Main 父项键名
	 * @param Son  子项键名
	 * @return 对应的消息
	 */
	public String getSon(String Main, String Son) {
		return getSon(Main, Son, new String[] {}, new String[] {});
	}

	/**
	 * 获取带子项的消息
	 * 
	 * @param Main    父项键名
	 * @param Son     子项键名
	 * @param MsgKey  要替换的键值
	 * @param MsgData 要替换成的内容
	 * @return 对应的消息
	 */
	public String getSon(String Main, String Son, String[] MsgKey, Object[] MsgData) {
		HashMap<String, String> map = new HashMap<>();
		if (msgconfig.get(Main) instanceof Map)
			map = (HashMap<String, String>) msgconfig.get(Main);
		return getText(map.get(Son), MsgKey, MsgData);
	}

	/**
	 * 将带有危险文本字符的字符串转换为不带危险字符
	 * 
	 * @param string 需要转换的字符串
	 * @return 传话完毕的字符串
	 */
	public static String reloadString(String string) {
		for (int i = 0; i < ReloadKey.length && i < ReloadData.length; i++)
			if (string.contains(ReloadKey[i]))
				string = string.replace(ReloadKey[i], ReloadData[i]);
		return string;
	}

	/**
	 * 读取语言文件中的特定内容并将其替换为所需要替换的内容后返回
	 * 
	 * @param Key     配置文件中村组文本数据的键
	 * @param MsgKey  所需要替换的变量
	 * @param MsgData 要替换成的内容
	 * @return 替换完毕的内容
	 */
	public String getMessage(String Key, String[] MsgKey, Object[] MsgData) {
		String msg = msgconfig.getString(Key, null);
		if (msg == null)
			return null;
		return getText(msg, MsgKey, MsgData);
	}

	/**
	 * 将指定内容替换变量
	 * 
	 * @param msg 要替换的内容
	 * @return
	 */
	public String getText(String msg) {
		return getText(msg, new String[] {}, new Object[] {});
	}

	/**
	 * 将指定内容替换变量
	 * 
	 * @param msg     字符内容
	 * @param MsgKey  要替换的变量名
	 * @param MsgData 替换后的内容
	 * @return
	 */
	public String getText(String msg, String[] MsgKey, Object[] MsgData) {
		if (msg == null) {
			plugin.getServer().getLogger().info(TextFormat.RED + "数据解析错误！建议检查消息文件或删除消息文件！");
			return msg;
		}
		for (int j1 = 0; j1 < Global_Key.length && j1 < Global_Data.length; j1++)
			if (msg.contains(Global_Key[j1]))
				msg = msg.replace(Global_Key[j1], Global_Data[j1]);
		for (int i = 0; i < MsgKey.length && i < MsgData.length; i++)
			if (msg.contains(MsgKey[i]))
				msg = msg.replace(MsgKey[i], String.valueOf(MsgData[i]));
		return msg;
	}
}
