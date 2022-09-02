package edu.gatech.zyin81.art_exhibition;
//
//public class Key_point {
//}
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Key_point {

//    public static String filepath;
    public int[][] recorded_loc;
    public String audio_file_path;
    public int[] centroid;
    public int is_recording;
    public int record_idx;
    public int num_responders;
    public boolean full;
    int record_size=1000; //change me

    public Key_point(String filepath) {
//            this.recorded_loc = new int[num_responders][200];
        this.audio_file_path = filepath;
        this.record_idx = 0;
        this.full = false;
        this.is_recording = -1;//No recordings yet
//        this.filepath=filepath;
    }

    public boolean record_points(int[] tdoa_vec) {
        if (this.record_idx < record_size) {
            for (int i = 0; i < this.num_responders; i++) {
                this.recorded_loc[i][this.record_idx] = tdoa_vec[i];
            }
            this.record_idx++;
            return true;
        } else {
            return false;
        }
    }

//    public void set_filepath() {
//        this.audio_file_path = this.audio_file_path;
//    }

    public boolean parse_new_line(String new_line) {
        //"5,1,1020,2,3420,3,3423,4,4546"

        String[] arrOfStr = new_line.split(",", 0);
        int size = arrOfStr.length;
        if (size % 2 == 0) {

            int max_num_responders = parse_int_specialchar(arrOfStr[0]);

            if (this.is_recording == -1) {
                this.num_responders = max_num_responders;
                this.recorded_loc = new int[num_responders][record_size];
                this.is_recording = 1;

            }
            if (this.is_recording == 1 ) { //Check we are in the recording state
                if (this.record_idx < record_size ) { //check we need to record more
                    if( (size-2)/2==this.num_responders) {
                        for (int i = 0; i < (size - 2) / 2; i++) {
                            int anchor_id = parse_int_specialchar(arrOfStr[2 * i + 1]);
                            if (anchor_id < this.num_responders + 1) {
                                this.recorded_loc[anchor_id - 1][this.record_idx] = parse_int_specialchar(arrOfStr[2 * i + 2]);
                                Log.w("recorded_loc: ",""+this.recorded_loc[anchor_id - 1][this.record_idx]);
                            }
                        }
                        this.record_idx++;
                    }
                    return true;
                } else {
                    this.is_recording = 0;  //finish recording
                    this.full = true;
                    this.compute_median();
                    return false;
                }
            } else {
                return false;
            }
        }
        else{
            return true;
        }
    }


    public int median(int[] l)
    {
        int[] lCopy=l.clone();
        Arrays.sort(lCopy);

        int middle = lCopy.length / 2;
        if (lCopy.length % 2 == 0)
        {
            long left = lCopy[middle - 1];
            long right = lCopy[middle];
            return (int) (left + right) / 2;
        }
        else
        {
            return lCopy[middle];
        }
    }

    public static int median_static(int[] l)
    {
        int[] lCopy=l.clone();
        Arrays.sort(lCopy);

        int middle = lCopy.length / 2;
        if (lCopy.length % 2 == 0)
        {
            long left = lCopy[middle - 1];
            long right = lCopy[middle];
            return (int) (left + right) / 2;
        }
        else
        {
            return lCopy[middle];
        }
    }

    public void compute_median()
    {
        this.centroid = new int[this.num_responders];
        for(int i=0;i<this.num_responders;i++)
        {
            centroid[i] = this.median(recorded_loc[i]);
        }
    }

    public double compute_distance(int[] tdoa_vec){
        double squared_dist = 0;
        int valid_tdoa_count = 0;
        for(int i=0;i<this.num_responders;i++)
        {
            if(tdoa_vec[i]!=0) {
                squared_dist = squared_dist + Math.pow((tdoa_vec[i] - this.centroid[i]), 2);
                valid_tdoa_count++;
            }
        }
        return Math.sqrt(squared_dist)/valid_tdoa_count;
    }

    public static boolean isNumeric(String strNum) {
        String numStr =strNum.replaceAll("[^a-zA-Z0-9]", "");
        if (numStr == null) {
            return true;
        }
        try {
            int d = Integer.parseInt(numStr);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static boolean allNumeric(String[] arr){
        for (int i = 0; i<arr.length; i++){
            if (isNumeric(arr[i])==false){
                return false;
            }
        }
        return true;
    }

    public static int parse_int_specialchar(String str)
    {
        return Integer.parseInt(str.replaceAll("[^0-9-]", ""));
    }

    public static boolean hasChar(String str){
        return str.matches(".*[a-zA-Z].*");
    }


    public static int[] parse_localization(String new_line) {
//        String[] arrOfStr = new_line.split(",", 0);
        String[] arrOfStr = new_line.split(",");
        int[] ret;
        int size = arrOfStr.length;
//        if (size % 2 == 0) {//changed
        if (size!=0 && size % 2 == 0 && !hasChar(arrOfStr[0]) && !hasChar(arrOfStr[size-1])) {//changed
//        if (size>4){
////        if((size-2)/2==this.num_responders) {
//            for (int i = 0; i < (size - 2) / 2; i++) {
//                int anchor_id = parse_int_specialchar(arrOfStr[2 * i + 1]);
//                if (anchor_id < this.num_responders + 1) {
//                    this.recorded_loc[anchor_id - 1][this.record_idx] = parse_int_specialchar(arrOfStr[2 * i + 2]);

            int max_num_responders = parse_int_specialchar(arrOfStr[0]);
            ret = new int[max_num_responders];
            ret[0]=1;

            for (int i = 0; i < (size - 2) / 2; i++) {
                ret[i]=2;
//                for (int i = 0; i < size / 2; i++) {
                int anchor_id = parse_int_specialchar(arrOfStr[2 * i + 1]);
                if (anchor_id < max_num_responders + 1) {
                    ret[anchor_id - 1] = parse_int_specialchar(arrOfStr[2 * i + 2]);
                }
            }
            return ret;
        }else{
            return null;
        }

    }

    public boolean load(String filepath){
        List<List<String>> records = new ArrayList<>();
        int num_entries = 0;
        int num_resp = 0;
        BufferedReader br ;
        try {
            br = new BufferedReader(new FileReader(filepath));
            String line=null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            while ( line!= null) {
                String[] values = line.split(",", 0);
                num_resp = values.length;
                records.add(Arrays.asList(values));
                num_entries++;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.num_responders = num_resp;
        this.recorded_loc =  new int[this.num_responders][num_entries];
        if(num_entries>0) {
            for (int i = 0; i < num_entries; i++) {
                for (int j = 0; j < this.num_responders; j++) {
                    this.recorded_loc[j][i] = Integer.parseInt(records.get(i).get(j));
                }
            }
            this.compute_median();
            this.full=true;
            return true;
        }else{
            return false;
        }
    }
}
