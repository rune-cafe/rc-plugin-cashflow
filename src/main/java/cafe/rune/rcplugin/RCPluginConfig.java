package cafe.rune.rcplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RuneCafe CashFlow")
public interface RCPluginConfig extends Config
{
	@ConfigItem(position = 1,
			keyName = "apiKey",
			name="API key",
			description = "The API key you got from rune.cafe.")
	default String apiKey() {
		return "";
	}

	@ConfigItem(position=2,
			keyName = "echoUploads",
			name = "Notify on Uploads",
			description = "Print a message to chat whenever data is sent to rune.cafe.")
	default boolean echoUploads() { return true; }

//	@ConfigItem(position=3,
//	keyName="useQa",
//	name="QA Mode",
//	description = "Use qa.rune.cafe.")
//	default boolean useQa() { return false; }
}
