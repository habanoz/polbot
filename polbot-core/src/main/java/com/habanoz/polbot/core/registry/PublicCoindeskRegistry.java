package com.habanoz.polbot.core.registry;

import com.habanoz.polbot.core.api.CoinDeskApi;
import com.habanoz.polbot.core.model.CoinDeskPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huseyina on 5/16/2017.
 */
@Component
public class PublicCoindeskRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PublicCoindeskRegistry.class);
    public static final String USD = "USD";
    public static final String TRY = "TRY";
    @Autowired
    private CoinDeskApi coinDeskApi;

    private static final int REFRESH_PERIOD_IN_MILLIS = 60000;

    private Map<String, CoinDeskPrice> btcPriceMap;
    private Date lastUpdate = new Date();

    @PostConstruct
    public void init() {
        btcPriceMap = new HashMap<>();
        btcPriceMap.put("TRY", coinDeskApi.getBtcPrice("TRY"));
        btcPriceMap.put("USD", coinDeskApi.getBtcPrice("USD"));
        lastUpdate = new Date();
    }

    private synchronized boolean update() {
        // if not up-to-date, refresh

        if (System.currentTimeMillis() - lastUpdate.getTime() > REFRESH_PERIOD_IN_MILLIS) {
            Map<String, CoinDeskPrice> _btcPriceMap = null;
            try {
                _btcPriceMap = new HashMap<>();
                _btcPriceMap.put(TRY, coinDeskApi.getBtcPrice(TRY));
                _btcPriceMap.put(USD, coinDeskApi.getBtcPrice(USD));
            } catch (Exception e) {
                logger.warn("Error while getting price map", e);
            }

            if (_btcPriceMap == null || _btcPriceMap.isEmpty() || _btcPriceMap.get(USD) == null)
                return false;

            btcPriceMap = _btcPriceMap;
            lastUpdate = new Date();
        }
        return true;
    }


    /**
     * returns last price map
     *
     * @return
     */
    public synchronized PricePack getBtcPriceMap() {
        update();
        return new PricePack(lastUpdate, btcPriceMap);
    }

    /**
     * returns only recent ticker map. If ticker is not up to date, throws exception
     *
     * @return
     * @throws RegistryDataNotUptoDateException
     */
    public synchronized PricePack getRecentPriceMap() throws RegistryDataNotUptoDateException {
        if (update())
            return new PricePack(lastUpdate, btcPriceMap);

        throw new RegistryDataNotUptoDateException();
    }

    public static class PricePack {
        private Date lastUpdate;
        private Map<String, CoinDeskPrice> btcPriceMap;

        public PricePack(Date lastUpdate, Map<String, CoinDeskPrice> btcPriceMap) {
            this.lastUpdate = lastUpdate;
            this.btcPriceMap = btcPriceMap;
        }

        public Date getLastUpdate() {
            return lastUpdate;
        }

        public Map<String, CoinDeskPrice> getBtcPriceMap() {
            return btcPriceMap;
        }
    }
}
