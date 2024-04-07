package ovh.bailon.foodnet2;

import java.util.ArrayList;

public interface OnDataEventListener {
    void onGetAllReady(ArrayList<OpenDating> list);

    void onGetReady(OpenDating openDating);
}
