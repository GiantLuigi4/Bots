package bots.rater;

import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Objects;

public class Messages
{
    public static String help=
"Current commands:\n" +
"`-rater:help`|Get a list of commands\n" +
"`-rate:user_{args} (user)`|Rate a person based on activity in all channels.\n" +
"`-rate:image` {args} `[image (or gif) attachment]`|Rates an image... idk how though.\n" +
"You do not need to ping, nor do you need the discriminator.\n" +
"When rating a user, any channel that starts with a `_` or includes `bot` will not be checked.\n" +
"`-rater:help [command]` for more info on said command.";
    
    public static EmbedBuilder buildHelp(String requester) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setAuthor(requester);
        builder.setColor(new Color(requester.length()%255, Math.abs(Objects.hash(requester))%255, Math.abs(Objects.hash(requester.toLowerCase()))%255));
        builder.addField("**-rater:help**","Get this message, or use `-rater:help {command}` to get help for a specific command.", false);
        builder.addField("**-rate:user**","Rate a user based off activity. You do not need to ping, and the results may be off if you do ping.", false);
        builder.addField("**-rate:image**","Rate an image ~~*somehow*~~.", false);
        builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
        return builder;
    }

    public static String helpImage=
"`rate:image`:\n" +
"`-rate:image` {args} `[image (or gif) attachment]`\n" +
"Valid inputs for {args}\n" +
"`-output:ratedImage` or `-output:rated`\n" +
"`-output:legacyRated` or `-output:legacy`\n" +
"`-output:decimal` or `-output:deci`\n" +
"`[empty]`\n" +
"`-output:` can be replaced with `-out:`\n" +
"Rating images has a balance,\n" +
"too complex, `<1` not complex enough, `<5` just right `>5`,\n" +
"virtually perfect, `10`.\n" +
"May or may not have a slight bias to red.";

    public static String helpUser=
"`rate:user`:\n" +
"`-rate:user_{args}` `(user)`\n" +
"Valid inputs for {args}\n" +
"`-ignore:server` or `-ignore:serv`\n" +
"`[empty]`\n" +
"It works off percent of the percent of chat in non bot spam channels that was your messages,\n" +
"not just a random value.";

    public static String version="V. 2.2";
}
