package github.pitbox46.epicfightnbt.network;

import com.google.gson.JsonObject;
import github.pitbox46.epicfightnbt.Config;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.network.NetworkEvent;

public class SSyncConfig implements IPacket {
    public JsonObject json;

    public SSyncConfig(JsonObject json) {
        this.json = json;
    }
    public SSyncConfig() {}

    @Override
    public SSyncConfig readPacketData(PacketBuffer buf) {
        this.json = JSONUtils.fromJson(buf.readString());
        return this;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeString(this.json.toString());
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Config.readJson(this.json);
    }
}
