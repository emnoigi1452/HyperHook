package me.stella.Discord;

import com.darkevan.PreventHopperOre.Utils.PlayerData;
import me.stella.Application.Library;
import me.stella.Minecraft.PreventHopper;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ElementBuilder {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    public static final NumberFormat formatter = NumberFormat.getInstance();

    public static MessageEmbed buildFullWarning(Map<String, Integer> full, long lastNode) {
        String title = "**Kho chứa đầy - " + full.size() + " loại**";
        List<MessageEmbed.Field> field = new ArrayList<>();
        field.add(new MessageEmbed.Field("**Cập nhật lúc**", sdf.format(Calendar.getInstance().getTime()), true));
        if(lastNode != -1) {
            long time = ((System.currentTimeMillis() - lastNode) / 1000L);
            field.add(new MessageEmbed.Field("**Lần cập nhật trước**", formatTime(time), true));
        }
        StringBuilder queryBuilder = new StringBuilder();
        for(String filled: full.keySet()) {
            String emote = Library.config.getEmote(filled);
            String capacity = formatter.format(full.get(filled));
            queryBuilder.append(emote).append(" [ Max: **").append(capacity).append("** ]").append("\n");
        }
        field.add(new MessageEmbed.Field("**Các khoáng sản đầy**", queryBuilder.toString(), false));
        return new MessageEmbed(null, title, null, EmbedType.UNKNOWN, OffsetDateTime.now(), 0xfc4e16, null, null, null, null, null, null, field);
    }

    public static MessageEmbed buildEchoDisplay(Map<String, String> display) {
        String title = "**Các cụm có kết nối kho**";
        List<MessageEmbed.Field> field = new ArrayList<>();
        field.add(new MessageEmbed.Field("**Cập nhật lúc**", sdf.format(Calendar.getInstance().getTime()), true));
        field.add(new MessageEmbed.Field("**Số cụm kết nối**", String.valueOf(display.keySet().size()), true));
        StringBuilder queryBuilder = new StringBuilder();
        display.forEach((proxy, id) -> {
            queryBuilder.append("**").append(proxy).append("** - Bot: ").append(id);
            queryBuilder.append("\n");
        });
        field.add(new MessageEmbed.Field("**Danh sách cụm**", queryBuilder.toString(), false));
        return new MessageEmbed(null, title, null, EmbedType.UNKNOWN, OffsetDateTime.now(), 0x2D7CFE, null, null, null, null, null, null, field);
    }

    public static String formatTime(long input) {
        long h = input / 3600;
        long m = (input - (h * 3600)) / 60;
        long s = input % 60;
        StringBuilder timeBuilder = new StringBuilder();
        if(h > 0)
            timeBuilder.append(h).append("h ");
        if(m > 0)
            timeBuilder.append(m).append("m ");
        if(s > 0)
            timeBuilder.append(s).append("s");
        String output = timeBuilder.toString();
        if(output.charAt(output.length() - 1) == ' ')
            return output.substring(0, output.length() - 1);
        return output;
    }

    public static String parseTimeName(String input) {
        int len = input.length();
        if(input.endsWith("h"))
            return input.substring(0, len - 1) + " giờ";
        else if(input.endsWith("m"))
            return input.substring(0, len - 1) + " phút";
        else
            return input.substring(0, len - 1) + " giây";
    }

    public enum ViewerLayout {
        NORMAL, PRICE, BLOCKS, INVALID;

        public static ViewerLayout node(String name) {
            switch(name.toLowerCase()) {
                case "normal":
                    return ViewerLayout.NORMAL;
                case "price":
                    return ViewerLayout.PRICE;
                case "blocks":
                    return ViewerLayout.BLOCKS;
                default:
                    return ViewerLayout.INVALID;
            }
        }
    }

    public static MessageEmbed buildEmbed(Player player, PlayerData data) {
        return buildEmbed(player, data, ViewerLayout.NORMAL);
    }

    public static MessageEmbed buildEmbed(Player player, PlayerData playerData, ViewerLayout layout) {
        String title = "**Kho khoáng sản - " + player.getName() + "**";
        List<MessageEmbed.Field> fields = new ArrayList<>();
        StringBuilder infoBuilder = new StringBuilder();
        fields.add(new MessageEmbed.Field("**Cập nhật lúc**", sdf.format(Calendar.getInstance().getTime()), true));
        fields.add(new MessageEmbed.Field("**Dung lượng kho**", formatter.format(playerData.getLimit("STONE")), true));
        for(String type: PreventHopper.keys) {
            String emote = Library.config.getEmote(type);
            int count = playerData.getBlock(type); String nodeDisplay; int limit = playerData.getLimit(type);
            switch(layout) {
                case BLOCKS:
                    int blocks = Math.floorDiv(count, 9);
                    String blockEmote = Library.config.getEmote(PreventHopper.fromKey(PreventHopper.toKey(type, false), true));
                    if(type.equals("STONE"))
                        nodeDisplay = emote + " " + formatter.format(count);
                    else
                        nodeDisplay = emote + " " + formatter.format(count) + " ~ [ **" + formatter.format(blocks) + "** " + blockEmote + " ]";
                    break;
                case PRICE:
                    double price = Library.preventHopper.getPrice(type) * count;
                    nodeDisplay = emote + " " + formatter.format(count) + " ~ [ **$" + formatter.format(price) + "** ]";
                    break;
                default:
                    nodeDisplay = emote + " " + formatter.format(count);
                    if(count == limit)
                        nodeDisplay += " [ **MAX** ]";
            }
            infoBuilder.append(nodeDisplay).append("\n");
        }
        fields.add(new MessageEmbed.Field("**Trạng thái**", infoBuilder.toString(), false));
        return new MessageEmbed(null, title, null, EmbedType.UNKNOWN, OffsetDateTime.now(), 0x34FFBE, null, null, null, null, null, null, fields);
    }

}
