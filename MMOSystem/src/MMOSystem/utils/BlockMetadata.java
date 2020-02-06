package MMOSystem.utils;

import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.plugin.Plugin;

public class BlockMetadata extends MetadataValue{

    public static BlockMetadata instance;

    public static BlockMetadata getInstance(){
        return instance;
    }

    public BlockMetadata(Plugin owningPlugin) {
        super(owningPlugin);
        instance = this;
    }

    public Plugin getOwningPlugin() {
        return this.owningPlugin.get();
    }

    public Object value(){
        return "mark";
    }

    public void invalidate(){

    }

}
