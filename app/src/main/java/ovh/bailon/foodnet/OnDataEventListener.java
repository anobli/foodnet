package ovh.bailon.foodnet;

import java.util.ArrayList;

public interface OnDataEventListener {
    void onGetAllReady(ArrayList<OpenDating> list);

    void onGetReady(OpenDating openDating);
}
