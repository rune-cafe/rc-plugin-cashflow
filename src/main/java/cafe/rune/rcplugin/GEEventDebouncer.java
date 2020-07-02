package cafe.rune.rcplugin;

import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.plugins.grandexchange.GrandExchangeOfferSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GEEventDebouncer implements Consumer<GrandExchangeOfferChanged> {
    private final Logger log = LoggerFactory.getLogger(GEEventDebouncer.class);
    private final Map<Integer, GrandExchangeOffer> state;
    private final Consumer<GrandExchangeOfferChanged> downstream;

    public GEEventDebouncer(Consumer<GrandExchangeOfferChanged> downstream) {
        this.downstream = downstream;
        state = new HashMap<>();
    }


    @Override
    public void accept(GrandExchangeOfferChanged event) {
        if(!state.containsKey(event.getSlot())) {
            digest(event);
            return;
        }

        if(event.getOffer() == null) {
            
        }

        GrandExchangeOffer cur = state.get(event.getSlot());
        GrandExchangeOffer next = event.getOffer();

        if(cur.getItemId() != next.getItemId()) {
            log.warn("Last item id for slot {} was {}, but just got an event with item id {}",
                    event.getSlot(), cur.getItemId(), next.getItemId());
            digest(event);
            return;
        }

        if(cur.getTotalQuantity() != next.getTotalQuantity()) {
            log.warn("Last offer for slot {} had total qty {}, but just got an event with total qty {}.",
                    event.getSlot(), cur.getTotalQuantity(), next.getTotalQuantity());
            digest(event);
            return;
        }


    }

    private void digest(GrandExchangeOfferChanged event) {
        state.put(event.getSlot(), event.getOffer());
        this.downstream.accept(event);
    }
}
