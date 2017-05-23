package com.habanoz.polbot.core.registry;

import com.habanoz.polbot.core.api.CoinDeskApi;
import com.habanoz.polbot.core.api.PoloniexPublicApi;
import com.habanoz.polbot.core.model.PoloniexTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Created by huseyina on 5/16/2017.
 */
@Component
public class PublicPoloniexTickerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PublicPoloniexTickerRegistry.class);
    @Autowired
    private PoloniexPublicApi publicApi;

    private static final int REFRESH_PERIOD_IN_MILLIS = 60000;

    private Map<String, PoloniexTicker> tickerMap;
    private Date lastTickerMapDate = new Date();

    @PostConstruct
    public void init() {
        tickerMap = Optional.ofNullable(publicApi.returnTicker()).orElse(Collections.emptyMap());
        lastTickerMapDate = new Date();
    }

    private synchronized boolean updateTickerMap() {
        // if not up-to-date, refresh
        if (System.currentTimeMillis() - lastTickerMapDate.getTime() > REFRESH_PERIOD_IN_MILLIS) {
            Map<String, PoloniexTicker> tmpTickerMap = null;
            try {
                tmpTickerMap = publicApi.returnTicker();
            } catch (Exception e) {
                logger.warn("Error while getting ticker map", e);
            }

            if (tmpTickerMap == null) return false;

            tickerMap = tmpTickerMap;
            lastTickerMapDate = new Date();
        }

        return true;
    }


    /**
     * returns last ticker map
     *
     * @return
     */
    public synchronized TickerPack getTickerMap() {
        updateTickerMap();
        return new TickerPack(lastTickerMapDate, tickerMap);
    }

    /**
     * returns only recent ticker map. If ticker is not up to date, throws exception
     *
     * @return
     * @throws RegistryDataNotUptoDateException
     */
    public synchronized TickerPack getRecentTickerMap() throws RegistryDataNotUptoDateException {
        if (updateTickerMap())
            return new TickerPack(lastTickerMapDate, tickerMap);

        throw new RegistryDataNotUptoDateException();
    }

    public static class TickerPack {
        private Date lastTickerMapDate;
        private Map<String, PoloniexTicker> tickerMap;

        public TickerPack(Date lastTickerMapDate, Map<String, PoloniexTicker> tickerMap) {
            this.lastTickerMapDate = lastTickerMapDate;
            this.tickerMap = tickerMap;
        }

        public Date getLastTickerMapDate() {
            return lastTickerMapDate;
        }

        public Map<String, PoloniexTicker> getTickerMap() {
            return tickerMap;
        }
    }
}
