package com.habanoz.polbot.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by habanoz on 05.04.2017.
 */
public class EmnCurrencyConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(EmnCurrencyConfigParser.class);

    private static final String SPLIT_SEPARATOR = ",";


    private static final int PARAM_BAL_PERCENT_INDEX = 0;
    private static final int PARAM_BUY_PERCENT_INDEX = 1;
    private static final int PARAM_SELL_PERCENT_INDEX = 2;
    private static final int PARAM_CURR_NAME_INDEX = 3;

    public List<EmnCurrencyConfig> parse(File configFile) {
        List<EmnCurrencyConfig> currencyConfigMap = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            String line = null;


            while ((line = bufferedReader.readLine()) != null) {
                String params[] = line.split(SPLIT_SEPARATOR);

                EmnCurrencyConfig emnCurrencyConfig = new EmnCurrencyConfig();
                emnCurrencyConfig.setCurrencyName(params[PARAM_CURR_NAME_INDEX].trim());
                emnCurrencyConfig.setUsableBalancePercent(Float.parseFloat(params[PARAM_BAL_PERCENT_INDEX].trim()));
                emnCurrencyConfig.setBuyOnPercent(Float.parseFloat(params[PARAM_BUY_PERCENT_INDEX].trim()));
                emnCurrencyConfig.setSellOnPercent(Float.parseFloat(params[PARAM_SELL_PERCENT_INDEX].trim()));

                currencyConfigMap.add(emnCurrencyConfig);
            }
        } catch (IOException e) {
            logger.warn("Error while parsing configuration file", e);
        }

        return currencyConfigMap;
    }
}
