package com.example.backendassignment.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiffUtil {

    /**
     * Compares old data and new data
     * Returns only changed fields
     */
    public static Map<String, Object> calculateDiff(
            Map<String, Object> oldData,
            Map<String, Object> newData) {

        Map<String, Object> diff = new HashMap<>();

        for (String key : newData.keySet()) {

            Object oldValue = oldData.get(key);
            Object newValue = newData.get(key);

            // If value changed
            if (!Objects.equals(oldValue, newValue)) {
                Map<String, Object> change = new HashMap<>();
                change.put("old", oldValue);
                change.put("new", newValue);

                diff.put(key, change);
            }
        }

        return diff;
    }
}
