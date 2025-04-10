package com.ridgebotics.ridgescout.ui.transfer;

import static androidx.navigation.fragment.FragmentKt.findNavController;
import static com.ridgebotics.ridgescout.utility.Colors.tba_current;
import static com.ridgebotics.ridgescout.utility.Colors.tba_next;
import static com.ridgebotics.ridgescout.utility.Colors.tba_previous;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAAddress;
import static com.ridgebotics.ridgescout.utility.FileEditor.TBAHeader;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.databinding.FragmentTransferTbaBinding;
import com.ridgebotics.ridgescout.ui.TBAEventOption;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.RequestTask;
import com.ridgebotics.ridgescout.utility.SettingsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TBASelectorFragment extends Fragment {

    private android.widget.TableLayout Table;
    private FragmentTransferTbaBinding binding;

    private final int year = SettingsManager.getYearNum();

    private ProgressDialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferTbaBinding.inflate(inflater, container, false);

        Table = binding.matchTable;

        Table.setStretchAllColumns(true);

        TableRow tr = new TableRow(getContext());
        addTableText(tr, "Loading Events...");
        Table.addView(tr);

        startLoading("Loading Events...");

        final RequestTask rq = new RequestTask();
        rq.onResult(s -> {
            if(s == null || s.isEmpty()) {
                AlertManager.error("Could not fetch event!");
                stopLoading();
                return null;
            }
            eventTable(s);
            return null;
        });
        rq.execute(TBAAddress + "events/"+year, TBAHeader);

        return binding.getRoot();
    }

    private void addTableText(TableRow tr, String textStr){
        TextView text = new TextView(getContext());
        text.setTextSize(18);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text align center
        text.setText(textStr);
        tr.addView(text);
    }

    public static int getEventTypeWeight(String type){
        switch(type){
            case "Preseason": return -3;
            case "District": return -2;
            case "Regional": return -1;
            case "District Championship Division": return 0;
            case "District Championship": return 1;
            case "Championship Divison": return 2;
            case "Championship Finals": return 3;
            case "Offseason": return 4;
        }

        return 0;
    }

    public void eventTable(String dataString){

        Table.removeAllViews();
        Table.setStretchAllColumns(true);
        Table.bringToFront();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date currentTime = Calendar.getInstance().getTime();

        try {
            JSONArray events = new JSONArray(dataString);

            List<JSONObject> data = new ArrayList<>();
            for(int i = 0; i < events.length(); i++){
                data.add(events.getJSONObject(i));
            }

            // Sort events by date, and then type
            data.sort((a, b) -> {
                try {
//                    return (int) (currentTime.getTime() - format.parse(a.getString("start_date")).getTime())
//                            -
//                            (int) (currentTime.getTime() - format.parse(b.getString("start_date")).getTime());

                    int diff = format.parse(a.getString("start_date")).compareTo(format.parse(b.getString("start_date"))) * 10;

                    if(diff == 0){
                        diff = getEventTypeWeight(a.getString("event_type_string")) - getEventTypeWeight(b.getString("event_type_string"));
                    }

                    if(diff == 0){
                        diff = a.getString("key").compareTo(b.getString("key"));
                    }

                    return diff;
                } catch (ParseException | JSONException e) {
                    AlertManager.error(e);
                    return 0;
                }
            });


            boolean toggle = false;

            for(int i=0;i<data.size();i++){
                JSONObject j = data.get(i);

                TBAEventOption row = new TBAEventOption(getContext());


//                TableRow tr = new TableRow(getContext());
//                TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
//                        TableRow.LayoutParams.WRAP_CONTENT,
//                        TableRow.LayoutParams.WRAP_CONTENT
//                );
//                rowParams.setMargins(20,20,20,20);
//                tr.setLayoutParams(rowParams);
//                tr.setPadding(20,20,20,20);
                row.setBackgroundColor(0x30000000);

                String name = j.getString("short_name");

                // Sometimes, a short name is not present on TBA Events
                if(name.isEmpty()){
                    name = j.getString("name");
                }

                String eventType = j.getString("event_type_string");
                if(eventType.equals("District") || eventType.equals("Regional"))
                    eventType = "Week " + (j.getInt("week")+1) + " " + eventType;

                row.setName(name);
                row.setCode(j.getString("key"));
                row.setType(eventType);

                try {
                    Date startDate = format.parse(j.getString("start_date"));
                    Date endDate = format.parse(j.getString("end_date"));
                    if(currentTime.after(endDate)){
                        row.setColor(tba_previous);
                    }else if(currentTime.before(startDate)){
                        row.setColor(tba_next);
                    }else if(currentTime.after(startDate) && currentTime.before(endDate)){
                        row.setColor(tba_current);
                    }
                } catch (Exception e) {
                    AlertManager.error("Failed finding start and end dates!", e);
                    stopLoading();
                }


                row.setOnClickListener(v -> {
                    TBAEventFragment.setEventData(j);
                    findNavController(this).navigate(R.id.action_navigation_tba_selector_to_navigation_tba_event);
                });

//                tr.addView(cl);
                Table.addView(row);


                toggle = !toggle;
            }

            stopLoading();
        }catch (JSONException j){
            AlertManager.error("Failed Downloading", j);
            stopLoading();
        }
    }
    private void startLoading(String title){
        getActivity().runOnUiThread(() -> {
            if(loadingDialog != null && loadingDialog.isShowing())
                loadingDialog.dismiss();
            loadingDialog = ProgressDialog.show(getActivity(), title, "Please wait...");
        });
    }

    private void stopLoading(){
        getActivity().runOnUiThread(() -> {
            if (loadingDialog != null)
                loadingDialog.cancel();
            loadingDialog = null;
        });
    }
}
