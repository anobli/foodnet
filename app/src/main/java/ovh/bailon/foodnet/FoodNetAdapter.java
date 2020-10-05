package ovh.bailon.foodnet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class FoodNetAdapter extends ArrayAdapter<OpenDating> {
    private FoodnetDBHelper db;
    private Context context;
    private ArrayList<OpenDating> list;

    public FoodNetAdapter(Context context, ArrayList<OpenDating> list, FoodnetDBHelper db) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
        this.db = db;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OpenDating openDating = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_food_net, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.food);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        ConstraintLayout layout = (ConstraintLayout) convertView.findViewById(R.id.layout);

        name.setText(openDating.getFood());
        date.setText(openDating.getExpDate());

        layout.setTag(position);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                OpenDating openDating = getItem(position);
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("foodnet")
                    .authority("foodnet.bailon.ovh")
                    .appendQueryParameter("id", "" + openDating.getID())
                    .appendQueryParameter("exit", "1");

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(builder.build());
                context.startActivity(i);
            }
        });

        ImageButton del = (ImageButton) convertView.findViewById(R.id.delete);
        del.setTag(position);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();

                OpenDating openDating = getItem(position);
                db.delete(openDating);
                list.clear();
                list.addAll(db.getAll());
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
