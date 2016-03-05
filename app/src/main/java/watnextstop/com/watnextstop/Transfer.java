package watnextstop.com.watnextstop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//todo: design recipe lol
public class Transfer {
    public double lat, lgt;
    public int stopNum;
    public String busHeader;
    public String lastLoc;

    public Transfer(double a, double b, int n, String s1, String s2) {
        lat = a;
        lgt = b;
        stopNum = n;
        busHeader = s1;
        lastLoc = s2;
    }
}
