package MMOSystem;


import MMOSystem.events.LevelUpEvent;
import MMOSystem.events.XpChangedEvent;
import cn.nukkit.Server;

import java.util.Map;

public class MMOPlayer {

    private String name;

    private Map<String, Object>  lists;

    public MMOPlayer(String name, Map<String, Object> lists){
        this.name = name;
        this.lists = lists;
    }

    public String getName(){
        return name;
    }

    public void addXP(String job, int xp){
        int xp_old = getXP(job);
        if(xp_old >= 1000*1000){
            return;
        }
        int xp_new = xp+ xp_old;
        if(xp_new > 1000*1000){
            xp_new = 1000*1000;
        }
        if(xp_new <= 0){
            xp_new = 0;
        }
        lists.put(job, xp_new);
        Server.getInstance().getPluginManager().callEvent(new XpChangedEvent(name));
        if((xp_new / 1000) != (xp_old / 1000)){
            int i;
            for(i = xp_old / 1000; i < xp_new / 1000; i++){
                Server.getInstance().getPluginManager().callEvent(new LevelUpEvent(name, i + 1, job));
            }
        }
    }

    public int getXP(String job){
        return (int)lists.getOrDefault(job, 0);
    }

    public int getLevel(String job){
        return getXP(job)/ 1000;
    }

    public void logout(){
        MMOSystem.getPugin().updateDatas("mmo", lists, "account", name);
    }

}
