package com.fernoaga.trading_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryMapConfig {

    @Value("${MAX_NUMBER_SYMBOLS}")
    private int MAX_NUMBER_SYMBOLS = 0;
    @Value("${MAP_LOAD_FACTOR}")
    private double MAP_LOAD_FACTOR = 0;

    /**
     * Calculates the initial size for a HashMap based on the maximum number of symbols and the load factor.
     * <p>
     * This method computes the initial size of a HashMap ensuring that when the map reaches the maximum number
     * the load factor is not higher then the configured MAP_LOAD_FACTOR load factor.
     * This is intended to provide sufficient capacity to minimize the need for resizing.
     * If the calculated size is zero or negative, a default size
     * of 100 is returned to ensure the map has a reasonable initial capacity.
     * </p>
     *
     * @return the calculated initial size for the HashMap, ensuring a minimum size of 100.
     */
    public int getHashMapSize() {
        int size = (int) (MAX_NUMBER_SYMBOLS * (1 + MAP_LOAD_FACTOR));
        return size > 0 ? size : 100;
    }
}
