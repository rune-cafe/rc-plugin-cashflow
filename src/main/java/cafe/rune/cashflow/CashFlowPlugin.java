package cafe.rune.cashflow;

import com.google.gson.Gson;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.cooking.CookingConfig;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static net.runelite.http.api.RuneLiteAPI.JSON;

@PluginDescriptor(
		name = "RuneCafe Cash Flow",
		description = "RuneCafe plugin providing RuneLite integration to track your cash flow on the GE.",
		tags = {"external", "integration", "prices", "trade"}
)
public class CashFlowPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CashFlowConfig config;

	@Provides
	CashFlowConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CashFlowConfig.class);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoadedEvent)
	{
		if(widgetLoadedEvent.getGroupId() != 383) {
			return;
		}

		Widget topGEWidget = client.getWidget(383, 0);
		Optional<Widget> optionalHistoryTitleWidget = findChildDepthFirst(topGEWidget, cw -> cw.getText().contains("Grand Exchange Trade History"));
		if(!optionalHistoryTitleWidget.isPresent()) {
			return;
		}

		Widget historyTitleWidget = optionalHistoryTitleWidget.get();
		clientThread.invokeLater(() -> {
			Widget[] geHistoryData = historyTitleWidget.getParent().getParent().getStaticChildren()[2].getDynamicChildren();

			List<GEHistoryRecord> records = new ArrayList<>();
			for(int i = 0; i < geHistoryData.length; i+=6) {
				records.add(new GEHistoryRecord(geHistoryData, i));
			}

			Gson gson = new Gson();
			System.out.println(gson.toJson(records));

			String urlString;
			try {
				urlString = "https://api.rune.cafe/api/gehistory/" + URLEncoder.encode(client.getLocalPlayer().getName(), "UTF-8") + "/snapshot";
			} catch(UnsupportedEncodingException e) {
				return;
			};

			Request request = new Request.Builder()
						.header("Authorization", "Bearer " + config.apiKey())
						.header("Accept", "application/json")
						.header("Content-Type", "application/json")
						.post(RequestBody.create(JSON, gson.toJson(records)))
						.url(HttpUrl.parse(urlString))
						.build();

			RuneLiteAPI.CLIENT.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					Logger.getLogger("cafe.rune.cashflow").log(Level.WARNING,"Error sending snapshot.", e);
				}

				@Override
				public void onResponse(Call call, Response response)
				{
					Logger.getLogger("cafe.rune.cashflow").info("Sent GE History snapshot.");
					Logger.getLogger("cafe.rune.cashflow").info(Integer.toString(response.code()));
					try {
						Logger.getLogger("cafe.rune.cashflow").info(response.body().string());
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
					response.close();
				}
			});

		});
	}

	private Optional<Widget> findChildDepthFirst(Widget root, Predicate<Widget> p) {
		if(p.test(root)) {
			return Optional.of(root);
		}


		Stream<Widget> children = Stream.concat(Stream.concat(
				Stream.of(root.getStaticChildren()),
				Stream.of(root.getNestedChildren())),
				Stream.of(root.getDynamicChildren()));

		return children
				.map(c -> findChildDepthFirst(c, p))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();

	}
}
