package MMOSystem;

import MMOSystem.commands.MMOCommand;
import MMOSystem.events.ChangeXpEvent;
import MMOSystem.events.LevelUpEvent;
import MMOSystem.events.XpChangedEvent;
import MMOSystem.managers.Manager;
import MMOSystem.managers.collect.*;
import MMOSystem.managers.damage.ArrowManager;
import MMOSystem.managers.damage.AxeManager;
import MMOSystem.managers.damage.SwordManager;
import MMOSystem.managers.sundry.AcrobaticsManager;
import MMOSystem.utils.DBUtil;
import MMOSystem.utils.Message;
import MMOSystem.utils.ReloadConfig;
import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.sql.SQLException;
import java.util.*;

public class MMOSystem extends PluginBase {


    public static MMOSystem mmosystem;


    /**
     * 经验配置文件
     */
    public Config xpconfig;
    public Config trconfig;

    /**
     * 各种自定义消息类对象
     */
    private Message message;

    public DBUtil db;

    public static MMOSystem getPugin(){
        return mmosystem;
    }

    private void registerCommands(){
        this.getServer().getCommandMap().register("mmo", new MMOCommand(this));
    }

    private void openManagers(){
        new MiningManager(this);
        new ExcavationManager(this);
        new FarmingManager(this);
        new GardeningManager(this);
        new WoodcuttingManager(this);
        new AcrobaticsManager(this);
        new FishingManager(this);
        new SwordManager(this);
        new AxeManager(this);
        new ArrowManager(this);
    }

    @Override
    public  void onLoad(){
        this.getServer().getLogger().info(this.getName() + "正在加载...");
        mmosystem = this;
        ReloadConfig.start();
        xpconfig = new Config(this.getDataFolder() + "/experience.yml", Config.YAML);
        trconfig = new Config(this.getDataFolder() + "/treasures.yml", Config.YAML);
        message = new Message(this);
    }

    @Override
    public void onEnable(){
        this.db = new DBUtil();
        this.db.makeConnection();
        try{
            this.db.updateByPreparedStatement(
                    "create table IF NOT EXISTS mmo(account varchar(64) not null, " +
                            "sword int default 0, " +
                            "arrow int default 0, " +
                            "axe int default 0, " +
                            "mining int default 0, " +
                            "excavation int default 0, " +
                            "farming int default 0, " +
                            "woodcutting int default 0, " +
                            "gardening int default 0, " +
                            "acrobatics int default 0, " +
                            "fishing int default 0, " +
                            "primary key (account));", null);
            this.db.updateByPreparedStatement("CREATE INDEX index_mmo_account ON mmo(account);", null);
        }catch (SQLException e){
            //
        }
        this.openManagers();
        this.registerCommands();

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }


    public String getJobCNName(String job){
        switch(job){
            case "mining":
                return MiningManager.getCNName();
            case "excavation":
                return ExcavationManager.getCNName();
            case "farming":
                return FarmingManager.getCNName();
            case "woodcutting":
                return WoodcuttingManager.getCNName();
            case "gardening":
                return GardeningManager.getCNName();
            case "acrobatics":
                return AcrobaticsManager.getCNName();
            case "sword":
                return SwordManager.getCNName();
            case "arrow":
                return ArrowManager.getCNName();
            case "axe":
                return AxeManager.getCNName();
            case "fishing":
                return FishingManager.getCNName();
            default:
                break;
        }
        return "";
    }

    public Manager getManager(String job){
        switch(job.toLowerCase()){
            case "mining":
                return MiningManager.getInstance();
            case "excavation":
                return ExcavationManager.getInstance();
            case "farming":
                return FarmingManager.getInstance();
            case "woodcutting":
                return WoodcuttingManager.getInstance();
            case "gardening":
                return GardeningManager.getInstance();
            case "acrobatics":
                return AcrobaticsManager.getInstance();
            case "sword":
                return SwordManager.getInstance();
            case "arrow":
                return ArrowManager.getInstance();
            case "axe":
                return AxeManager.getInstance();
            case "fishing":
                return FishingManager.getInstance();
            default:
                break;
        }
        return null;
    }

    public void registerPlayer(Player p){
        LinkedHashMap<String, Object> settings = new LinkedHashMap<>();
        settings.put("account", p.getName());
        insertData("mmo", settings, true);
    }

    private HashMap<String, MMOPlayer> players = new HashMap<String, MMOPlayer>();

    public void loginPlayer(Player p){
        String name = p.getName();
        Map<String, Object> columns = getData("mmo", "account", name);
        if(columns != null){
            columns.remove("account");
            players.put(name, new MMOPlayer(name, columns));
        }
    }

    public void logoutPlayer(Player p){
        MMOPlayer mp = players.get(p.getName());
        if(mp != null){
            mp.logout();
            players.remove(p.getName());
        }
    }

    private int getXp2LevelRate(String job){
        return 1000;
    }

    public int getLevel(Player p, String job){
        return getLevel(p.getName(), job);
    }

    public int getLevel(String name, String job){
        MMOPlayer mp = players.get(name);
        if(mp != null){
            return mp.getLevel(job);
        }else{
            if(name != null && job != null){
                Object xp = getData("mmo", "account", name, job);
                if(xp != null){
                    return  (int)xp/ getXp2LevelRate(job);
                }
            }
            return 0;
        }
    }

    public int getXp(Player p, String job){
        return getXp(p.getName(), job);
    }

    public int getXp(String name, String job){
        MMOPlayer mp = players.get(name);
        if(mp != null){
            return mp.getXP(job);
        }else {
            Object xp = getData("mmo", "account", name, job);
            if (xp != null) {
                return (int) xp;
            }
            return 0;
        }
    }

    public void addXp(Player p, String job, int xp){
        addXp(p.getName(), job, xp);
        //p.sendPopup("", "§f<§e" + getJobCNName(job) + "§f> +§3" +xp);
    }

    public void addXp(String name, String job, int xp){
        if(xp == 0){
            return;
        }
        ChangeXpEvent event = new ChangeXpEvent(name, job, xp);
        this.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()){
            MMOPlayer mp = players.get(name);
            if(mp != null){
                mp.addXP(job, event.getXp());
            }else{
                int xp_old = getXp(name, job);
                if(xp_old >= 1000*1000){
                    return;
                }
                int xp_new = event.getXp()+ xp_old;
                if(xp_new > 1000*1000){
                    xp_new = 1000*1000;
                }
                if(xp_new <= 0){
                    xp_new = 0;
                }
                updateData("mmo", job, xp_new, "account", name);
                this.getServer().getPluginManager().callEvent(new XpChangedEvent(name));
                if((xp_new / 1000) != (xp_old / 1000)){
                    int i;
                    for(i = xp_old / 1000; i < xp_new / 1000; i++){
                        this.getServer().getPluginManager().callEvent(new LevelUpEvent(name, i + 1, job));
                    }
                }
            }

        }
    }
    /*-----------------------语言转换-----------------------*/

    public Message getMessage(){
        return message;
    }

    public String getMessage(String Key) {
        return message.getMessage(Key, new String[] {}, new Object[] {});
    }

    public String getMessage(String Key, String[] MsgKey, Object[] MsgData) {
        return message.getMessage(Key, MsgKey, MsgData);
    }

    /*-----------------------数据操作-----------------------*/

    public boolean insertData(String table, LinkedHashMap<String, Object> settings, Boolean ignore){
        StringBuilder sql = new StringBuilder("INSERT ");
        if(ignore){
            sql.append("INTO ");
        }else{
            sql.append("INTO ");
        }
        sql.append(table);
        sql.append(" (");
        Set keys = settings.keySet();
        for (Object k : keys) {
            sql.append(k);
            sql.append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append(") VALUES (");
        for (int i = 1; i <= settings.size(); i++) {
            sql.append("?, ");
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append(")");
        try {
            this.db.updateByPreparedStatement(sql.toString(), new ArrayList<>(settings.values()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateData(String table, String k1, Object v1, String k2, Object v2) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ").append(k1).append(" = ? WHERE ").append(k2).append(" = ?");
        try {
            List<Object> params = new ArrayList<>();
            params.add(v1);
            params.add(v2);
            this.db.updateByPreparedStatement(sql.toString(), params);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateDatas(String table, Map<String, Object> settings, String k, Object v) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        List<Object> params = new ArrayList<>();
        for(Map.Entry<String, Object> entry : settings.entrySet()){
            sql.append(entry.getKey()).append("=?, ");
            params.add(entry.getValue());
        }
        sql.deleteCharAt(sql.length() - 2).append("WHERE ").append(k).append(" = ?");
        params.add(v);
        try {
            this.db.updateByPreparedStatement(sql.toString(), params);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, Object> getData(String table, String k, Object v){
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table).append(" WHERE ").append(k).append(" = ?");
        List<Object> params = new ArrayList<>();
        params.add(v);
        try{
            Map<String, Object>result =  this.db.findSimpleResult(sql.toString(), params);
            if(result == null || result.isEmpty()){
                return null;
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    public Object getData(String table, String k, Object v, String column){
        StringBuilder sql = new StringBuilder("SELECT ").append(column).append(" FROM ").append(table).append(" WHERE ").append(k).append(" = ?");
        List<Object> params = new ArrayList<>();
        params.add(v);
        try{
            Map<String, Object>result =  this.db.findSimpleResult(sql.toString(), params);
            if(result == null || result.isEmpty()){
                return null;
            }
            return result.get(column);
        }catch (Exception e){
            return null;
        }
    }
}
