package ovh.bailon.foodnet;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FoodNetListActivity extends AppCompatActivity {
    private FoodnetDBHelper db;
    private final ArrayList<OpenDating> netList = new ArrayList<OpenDating>();
    private ArrayAdapter<OpenDating> listViewAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_net_list);

        db = new FoodnetDBHelper(this);
        listView = findViewById(R.id.FoodNetList);
        listViewAdapter = new FoodNetAdapter(this, this.netList, db);
        this.listView.setAdapter(this.listViewAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        netList.clear();
        netList.addAll(db.getAll());
        listViewAdapter.notifyDataSetChanged();
    }
}