package com.ridgebotics.ridgescout.ui.transfer;

import static com.ridgebotics.ridgescout.utility.Colors.fileselector_selected_color;
import static com.ridgebotics.ridgescout.utility.Colors.fileselector_unselected_color;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTransferFileSelectorBinding;
import com.ridgebotics.ridgescout.types.ScoutingFile;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.DataManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to show a file browseer
public class FileSelectorFragment extends Fragment {

    private static on_file_select onSelect = files -> {};

    public static void setOnSelect(on_file_select tmp){onSelect = tmp;}

    public interface on_file_select {
        void onSelect(byte[] data);
    }

    FragmentTransferFileSelectorBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransferFileSelectorBinding.inflate(inflater, container, false);

        DataManager.reload_event();

        meta_string_array = new String[]{
                "matches.fields",
                "pits.fields",
                evcode+".eventdata"
        };

        String[] files = FileEditor.getEventFiles(evcode);

        Boolean[] selected_arr = new Boolean[files.length];
        Arrays.fill(selected_arr, Boolean.TRUE);

        for(int i = 0; i < files.length; i++){
            TableRow tr = new TableRow(getContext());
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(20,20,20,20);
            tr.setLayoutParams(rowParams);
            tr.setPadding(20,20,20,20);
            binding.fileSelectorTable.addView(tr);

            tr.setBackgroundColor(fileselector_selected_color);

            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setChecked(true);
            tr.addView(checkBox);

            TextView tv = new TextView(getContext());
            tv.setText(String.valueOf(files[i]));
            tv.setTextSize(20);
            tr.addView(tv);

            final int fi = i;
            tr.setOnClickListener(view -> {
                boolean sel = !selected_arr[fi];
                selected_arr[fi] = sel;

                tr.setBackgroundColor(sel ? fileselector_selected_color : fileselector_unselected_color);
                ((CheckBox) tr.getChildAt(0)).setChecked(sel);
            });
            checkBox.setOnClickListener(view -> {
                boolean sel = !selected_arr[fi];
                selected_arr[fi] = sel;

                tr.setBackgroundColor(sel ? fileselector_selected_color : fileselector_unselected_color);
                ((CheckBox) tr.getChildAt(0)).setChecked(sel);
            });
        }

        binding.fileSelectorSearchbar.setOnKeyListener((view, a, keyEvent) -> {

            String search_param = binding.fileSelectorSearchbar.getText().toString();
            List<Integer> match_num_nums = get_matches_from_search_params(search_param);

            Arrays.fill(selected_arr, Boolean.TRUE);

            for(int i = 0; i < files.length; i++){
                TableRow child = (TableRow) binding.fileSelectorTable.getChildAt(i);
                child.setBackgroundColor(fileselector_selected_color);
                boolean sel = is_in_search_param(files[i], search_param, match_num_nums);
                child.setVisibility(sel ? View.VISIBLE : View.GONE);
                ((CheckBox) child.getChildAt(0)).setChecked(sel);

            }

            return false;
        });

        binding.fileSelectorButton.setText("Send");
        binding.fileSelectorButton.setOnClickListener(view -> {
            List<String> filenames = new ArrayList<>();
            for(int i = 0; i < files.length; i++){
                View child = binding.fileSelectorTable.getChildAt(i);
                if(child.getVisibility() == View.VISIBLE && selected_arr[i])
                    filenames.add(files[i]);

            }
            onSelect.onSelect(get_bytes_of_filenames(filenames));
        });


        return binding.getRoot();
    }



    private static String[] meta_string_array;

    private boolean is_in_search_param(String filename, String search_param, List<Integer> nums){
        return
                ("meta".contains(search_param) && Arrays.asList(meta_string_array).contains(filename)) ||
                        filename.contains(search_param) ||
                        match_file_is_match_num(filename, nums);
    }


    private boolean match_file_is_match_num(String filename, List<Integer> nums){
        if(!filename.endsWith(".matchscoutdata")) return false;
        String[] dash_split = filename.split("-");
        if(dash_split.length != 5) return false;
        String s = dash_split[1];
        if(!is_int(s)) return false;
        int n = Integer.parseInt(s);
        return nums.contains(n);
    }

    private List<Integer> get_matches_from_search_params(String search_param){
        List<Integer> nums = new ArrayList<>();
        String[] comma_split = search_param.split(",");

        for(int i = 0; i < comma_split.length; i++){
            if(comma_split[i].contains("-")){

                String[] dash_split = comma_split[i].split("-");
                if(dash_split.length != 2) continue;
                String stra = dash_split[0];
                String strb = dash_split[1];

                if(!(is_int(stra) && is_int(strb))) continue;

                int a = Integer.parseUnsignedInt(stra);
                int b = Integer.parseUnsignedInt(strb);

                for(int x = a; x <= b; x++)
                    nums.add(x);
            } else if(is_int(comma_split[i]))
                nums.add(Integer.parseUnsignedInt(comma_split[i]));
        }

        return nums;
    }

    private boolean is_int(String num){
        try {
            Integer.parseUnsignedInt(num);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }



    private static byte[] get_bytes_of_filenames(List<String> filenames){
        try {
            ByteBuilder b = new ByteBuilder();

            for(int i = 0; i < filenames.size(); i++){
                ScoutingFile f = new ScoutingFile(filenames.get(i));

                if(!FileEditor.fileExist(f.filename)) {
                    AlertManager.addSimpleError("File " + f.filename + " Does not exist!");
                    continue;
                };

                byte[] bytes = f.encode();

                if(bytes == null || bytes.length == 0) {
                    AlertManager.addSimpleError("File " + f.filename + " Has no data!");
                    continue;
                };

                b.addRaw(ScoutingFile.typecode, bytes);
            }

            return b.build();
        } catch (ByteBuilder.buildingException e){
            AlertManager.error(e);
            return null;
        }
    }
}
