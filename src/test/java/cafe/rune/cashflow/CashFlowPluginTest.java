package cafe.rune.cashflow;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CashFlowPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CashFlowPlugin.class);
		RuneLite.main(args);
	}
}