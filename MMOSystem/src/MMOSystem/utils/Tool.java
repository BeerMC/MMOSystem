package MMOSystem.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.DyeColor;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
/**
 * @author Winfxk
 */
public class Tool {

	public static void spawnFirework(Player player) {
		Level level = player.getLevel();
		ItemFirework item = new ItemFirework();
		CompoundTag tag = new CompoundTag();
		Random random = new Random();
		CompoundTag ex = new CompoundTag();
		ex.putByteArray("FireworkColor",new byte[]{
				(byte) DyeColor.values()[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
		});
		ex.putByteArray("FireworkFade",new byte[0]);
		ex.putBoolean("FireworkFlicker",random.nextBoolean());
		ex.putBoolean("FireworkTrail",random.nextBoolean());
		ex.putByte("FireworkType",ItemFirework.FireworkExplosion.ExplosionType.values()
				[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
		tag.putCompound("Fireworks",(new CompoundTag("Fireworks")).putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight",1));
		item.setNamedTag(tag);
		CompoundTag nbt = new CompoundTag();
		nbt.putList(new ListTag<DoubleTag>("Pos")
				.add(new DoubleTag("",player.x+0.5D))
				.add(new DoubleTag("",player.y+0.5D))
				.add(new DoubleTag("",player.z+0.5D))
		);
		nbt.putList(new ListTag<DoubleTag>("Motion")
				.add(new DoubleTag("",0.0D))
				.add(new DoubleTag("",0.0D))
				.add(new DoubleTag("",0.0D))
		);
		nbt.putList(new ListTag<FloatTag>("Rotation")
				.add(new FloatTag("",0.0F))
				.add(new FloatTag("",0.0F))

		);
		nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
		EntityFirework entity = new EntityFirework(level.getChunk((int)player.x >> 4, (int)player.z >> 4), nbt);
		entity.spawnToAll();
	}


	private static String colorKeyString = "123456789abcdef";
	private static String randString = "0123456789-+abcdefghijklmnopqrstuvwxyz_=";

	//SHA512加密(32个字符)
	public static String hash(String pwd) {
		pwd = pwd + "foansd";
		String shaPwd = null;
		if (pwd != null && pwd.length() > 0) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
				messageDigest.update(pwd.getBytes());
				byte byteBuffer[] = messageDigest.digest();
				StringBuilder strHexString = new StringBuilder();
				for (int i = 0; i < byteBuffer.length; i++) {
					String hex = Integer.toHexString(0xff & byteBuffer[i]);
					if (hex.length() == 1) {
						strHexString.append('0');
					}
					strHexString.append(hex);
				}
				shaPwd = strHexString.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return shaPwd.substring(0, 32);
	}

	/**
	 * 获取从一个时间点到现在的时间差
	 * 
	 * @param time 时间点
	 * @return
	 */
	public static String getTimeTo(Temporal time) {
		return getTimeBy(Duration.between(time, Instant.now()).toMillis() / 1000);
	}

	/**
	 * 将秒长度转换为日期长度
	 * 
	 * @param time 秒长度
	 * @return
	 */
	public static String getTimeBy(double time) {
		int y = (int) (time / 31556926);
		time = time % 31556926;
		int d = (int) (time / 86400);
		time = time % 86400;
		int h = (int) (time / 3600);
		time = time % 3600;
		int i = (int) (time / 60);
		double s = (double) (time % 60);
		return (y > 0 ? y + "年" : "") + (d > 0 ? d + "天" : "") + (h > 0 ? h + "小时" : "") + (i > 0 ? i + "分钟" : "")
				+ (s > 0 ? Double2(s, 1) + "秒" : "");
	}

	/**
	 * 判断两个ID是否匹配，x忽略匹配
	 * 
	 * @param ID1 第一个ID
	 * @param ID2 第二个ID
	 * @return
	 */
	public static boolean isMateID(String ID1, String ID2) {
		if (ID1 == null || ID2 == null)
			return false;
		if (!ID1.contains(":"))
			ID1 += ":0";
		if (!ID2.contains(":"))
			ID2 += ":0";
		String[] ID1s = ID1.split(":"), ID2s = ID2.split(":");
		if (ID1s[0].equals("x") || ID2s[0].equals("x") || ID1s[0].equals(ID2s[0]))
			if (ID1s[1].equals("x") || ID2s[1].equals("x") || ID2s[1].equals(ID1s[1]))
				return true;
			else
				return false;
		else
			return false;
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static String getTime() {
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		return time.format(new Date());
	}

	/**
	 * 返回当前时间 <年-月-日>
	 * 
	 * @return
	 */
	public static String getDate() {
		SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd");
		return data.format(new Date());
	}

	/**
	 * 自动检查ID是否包含特殊值，若不包含则默认特殊值为0后返回数组
	 * 
	 * @param ID 要检查分解的ID
	 * @return int[]{ID, Damage}
	 */
	public static int[] IDtoFullID(String ID) {
		return IDtoFullID(ID, 0);
	}

	/**
	 * 自动检查ID是否包含特殊值，若不包含则设置特殊值为用户定义值后返回数组
	 * 
	 * @param ID     要检查的ID
	 * @param Damage 要默认设置的特殊值
	 * @return int[]{ID, Damage}
	 */
	public static int[] IDtoFullID(String ID, int Damage) {
		if (ID.contains(":"))
			ID += ":" + Damage;
		String[] strings = ID.split(":");
		return new int[] { Integer.valueOf(strings[0]), Integer.valueOf(strings[1]) };
	}

	/**
	 * 获取随机数
	 * 
	 * @param min 随机数的最小值
	 * @param max 随机数的最大值
	 */
	public static int getRand(int min, int max) {
		if(min > max){
			return ThreadLocalRandom.current().nextInt(max, min-max);
		}
		return ThreadLocalRandom.current().nextInt(min, max-min);
	}


	/**
	 * 输入几率(0~100)，输出随机结果
	 *
	 * @param chance 随机数的最小值
	 */
	public static boolean YorN(double chance){
		if(chance<0){
			chance = 0;
		}
		if(chance > 100){
			chance = 100;
		}

		return chance >= getRand(0, 100);
	}

	/**
	 * 输入几率(0~100)，输出随机结果
	 *
	 * @param chance 随机数的最小值
	 */
	public static boolean YorN(int chance){
		if(chance<0){
			chance = 0;
		}
		if(chance > 100){
			chance = 100;
		}

		return chance >= getRand(0, 100);
	}


	/**
	 * 返回一个随机颜色代码
	 * 
	 * @return
	 */
	public static String getRandColor() {
		return getRandColor(colorKeyString);
	}

	/**
	 * 返回一个随机颜色代码
	 * 
	 * @param ColorFont 可以随机到的颜色代码
	 * @return
	 */
	public static String getRandColor(String ColorFont) {
		int rand = Tool.getRand(0, ColorFont.length() - 1);
		return "§" + ColorFont.substring(rand, rand + 1);
	}

	/**
	 * 将字符串染上随机颜色
	 * 
	 * @param Font 要染色的字符串
	 * @return
	 */
	public static String getColorFont(String Font) {
		return getColorFont(Font, colorKeyString);
	}

	/**
	 * 返回一个随机字符
	 * 
	 * @return 随机字符
	 */
	public static String getRandString() {
		return getRandString(randString);
	}

	/**
	 * 返回一个随机字符
	 * 
	 * @param string 要随机字符的范围
	 * @return 随机字符
	 */
	public static String getRandString(String string) {
		int r1 = getRand(0, string.length() - 1);
		return string.substring(r1, r1 + 1);
	}

	/**
	 * 将字符串染上随机颜色
	 * 
	 * @param Font      要染色的字符串
	 * @param ColorFont 随机染色的颜色代码
	 * @return
	 */
	public static String getColorFont(String Font, String ColorFont) {
		String text = "";
		for (int i = 0; i < Font.length(); i++) {
			int rand = Tool.getRand(0, ColorFont.length() - 1);
			text += "§" + ColorFont.substring(rand, rand + 1) + Font.substring(i, i + 1);
		}
		return text;
	}

	/**
	 * 判断字符串是否是整数型
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		try {
			Float.valueOf(str).intValue();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断一段字符串中是否只为纯数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		String bigStr;
		try {
			bigStr = new BigDecimal(str).toString();
		} catch (Exception e) {
			return false;//异常 说明包含非数字。
		}
		return true;
	}

	/**
	 * 字符串转换Unicode
	 * 
	 * @param string 要转换的字符串
	 * @return
	 */
	public static String StringToUnicode(String string) {
		StringBuffer unicode = new StringBuffer();
		for (int i = 0; i < string.length(); i++)
			unicode.append("\\u" + Integer.toHexString(string.charAt(i)));
		return unicode.toString();
	}

	/**
	 * unicode 转字符串
	 * 
	 * @param unicode 全为 Unicode 的字符串
	 * @return
	 */
	public static String UnicodeToString(String unicode) {
		StringBuffer string = new StringBuffer();
		String[] hex = unicode.split("\\\\u");
		for (int i = 1; i < hex.length; i++)
			string.append((char) Integer.parseInt(hex[i], 16));
		return string.toString();
	}

	/**
	 * 将Map按数据升序排列
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int compare = (o1.getValue()).compareTo(o2.getValue());
				return -compare;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 设置小数长度</br>
	 * 默认保留两位小数</br>
	 * 
	 * @param d 要设置的数值
	 * @return
	 */
	public static double Double2(double d) {
		return Double2(d, 2);
	}

	/**
	 * 设置小数长度</br>
	 * 
	 * @param d      要设置的数
	 * @param length 要保留的小数的长度
	 * @return
	 */
	public static double Double2(double d, int length) {
		String s = "#.0";
		for (int i = 1; i < length; i++)
			s += "0";
		DecimalFormat df = new DecimalFormat(s);
		return Double.valueOf(df.format(d));
	}

	/**
	 * 发送HTTP请求
	 * 
	 * @param httpUrl 请求地址
	 * @param param   请求的内容
	 * @return
	 */
	public static String doPost(String httpUrl, String param) {
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream os = null;
		BufferedReader br = null;
		String result = null;
		try {
			URL url = new URL(httpUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(60000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
			os = connection.getOutputStream();
			os.write(param.getBytes());
			if (connection.getResponseCode() == 200) {
				is = connection.getInputStream();
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				StringBuffer sbf = new StringBuffer();
				String temp = null;
				while ((temp = br.readLine()) != null) {
					sbf.append(temp);
					sbf.append("\r\n");
				}
				result = sbf.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (null != os)
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (null != is)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			connection.disconnect();
		}
		return result;
	}

	/**
	 * 从一段字符内截取另一段字符
	 * 
	 * @param Context 要截取字符的原文
	 * @param strStart   要截取的第一段文字
	 * @param strEnd   要截取的第二段文字
	 * @return 截取完毕的内容
	 */
	public static String cutString(String Context, String strStart, String strEnd) {
		int strStartIndex = Context.indexOf(strStart);
		int strEndIndex = Context.lastIndexOf(strEnd);
		if (strStartIndex < 0) {
			return null;
		}
		if (strEndIndex < 0) {
			return null;
		}
		return Context.substring(strStartIndex, strEndIndex).substring(strStart.length());
	}

}
