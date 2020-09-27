package com.infocomm.logpond_v2.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ArrayListSortingManager {

    public static ArrayList sortJsonObject(ArrayList arrayList, final String key, boolean isAsc) throws Exception {
        List<JSONObject> jsons = arrayList;
        if(isAsc){
        	Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    try {
                        String lid = lhs.getString(key);
                        String rid = rhs.getString(key);

                        return lid.compareToIgnoreCase(rid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Here you could parse string id to integer and then compare.
                    return 0;
                }
            });
        }else{
        	Collections.sort(jsons, Collections.reverseOrder(new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    try {
                        String lid = lhs.getString(key);
                        String rid = rhs.getString(key);

                        return lid.compareToIgnoreCase(rid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Here you could parse string id to integer and then compare.
                    return 0;
                }
            }));
        }
        
        return arrayList;
    }

    public static String filterSpecialChar(String word){
        //html url encoding
        String text = word.replace("%", "%25");
        text = text.replace(" ", "%20");
        text = text.replace("@", "%40");
        text = text.replace("#", "%23");
        text = text.replace("$", "%24");
        text = text.replace("^", "%5E");
        text = text.replace("&", "%26");
        text = text.replace("'", "%27");
        text = text.replace("=", "%3D");
        text = text.replace("+", "%2B");
        text = text.replace(":", "%3A");
        text = text.replace(";", "%3B");
        text = text.replace("\"", "%22");
        text = text.replace("\\", "%5C");
        text = text.replace("/", "%2F");
        text = text.replace("?", "%3F");
        text = text.replace("<", "%3C");
        text = text.replace(">", "%3E");
        text = text.replace("[", "%5B");
        text = text.replace("]", "%5D");
        text = text.replace("{", "%7B");
        text = text.replace("}", "%7D");
        text = text.replace("\'", "%60");
        return text;
    }

    public static String encodeSpecialChar(String word){
        //html url encoding
        String text = word.replace("%25", "%");
        text = text.replace("%20", " ");
        text = text.replace("%40", "@");
        text = text.replace("%23", "#");
        text = text.replace("%24", "$");
        text = text.replace("%5E", "^");
        text = text.replace("%26", "&");
        text = text.replace("%27", "&#39;");
        text = text.replace("%3D", "=");
        text = text.replace("%2B", "+");
        text = text.replace("%3A", ":");
        text = text.replace("%3B", ";");
        text = text.replace("%22", "\"");
        text = text.replace("%5C", "\\");
        text = text.replace("%2F", "/");
        text = text.replace("%3F", "?");
        text = text.replace("%3C", "<");
        text = text.replace("%3E", ">");
        text = text.replace("%5B", "[");
        text = text.replace("%5D", "]");
        text = text.replace("%7B", "{");
        text = text.replace("%7D", "}");
        text = text.replace("%60", "\'");
        text = text.replace("&#39;", "'");
        return text;
    }
}
