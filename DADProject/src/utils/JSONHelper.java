package utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
	 /**
     * Parse a JSON string into a JSONObject.
     * @param jsonText JSON text representation of an object
     * @return JSONObject instance
     * @throws JSONException if parsing fails
     */
    public static JSONObject parseObject(String jsonText) throws JSONException {
        return new JSONObject(jsonText);
    }

    /**
     * Parse a JSON string into a JSONArray.
     * @param jsonText JSON text representation of an array
     * @return JSONArray instance
     * @throws JSONException if parsing fails
     */
    public static JSONArray parseArray(String jsonText) throws JSONException {
        return new JSONArray(jsonText);
    }

    /**
     * Convert a JSONObject to its string representation.
     * @param obj JSONObject to convert
     * @return JSON string
     */
    public static String toString(JSONObject obj) {
        return obj.toString();
    }

    /**
     * Convert a JSONArray to its string representation.
     * @param arr JSONArray to convert
     * @return JSON string
     */
    public static String toString(JSONArray arr) {
        return arr.toString();
    }

    /**
     * Map a JSONArray to a list of typed objects.
     * @param <T>    target type
     * @param arr    source JSONArray
     * @param mapper callback to convert JSONObject to T
     * @return List of mapped objects
     */
    public static <T> List<T> toList(JSONArray arr, JSONMapper<T> mapper) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(mapper.map(arr.getJSONObject(i)));
        }
        return list;
    }

    /**
     * Convert a list of JSONConvertible objects to a JSONArray.
     * @param list list of objects implementing JSONConvertible
     * @return JSONArray
     */
    public static JSONArray fromList(List<? extends JSONConvertible> list) {
        JSONArray arr = new JSONArray();
        for (JSONConvertible item : list) {
            arr.put(item.toJSON());
        }
        return arr;
    }

    /**
     * Functional interface for mapping a JSONObject to a typed object.
     */
    public interface JSONMapper<T> {
        T map(JSONObject json) throws JSONException;
    }

    /**
     * Interface to be implemented by model classes that can convert themselves to JSONObject.
     */
    public interface JSONConvertible {
        JSONObject toJSON();
    }
}
