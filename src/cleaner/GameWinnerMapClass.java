package cleaner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class GameWinnerMapClass extends Mapper<LongWritable, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private final static IntWritable ten = new IntWritable(10);
    private Text word = new Text();

    private static ArrayList<String> columns = new ArrayList<String>(Arrays.asList(
    								"","Game_Id","Date","Period","Event","Description","Time_Elapsed","Seconds_Elapsed","Strength",
    								"Ev_Zone","Type","Ev_Team","Home_Zone","Away_Team","Home_Team",
    								"p1_name","p1_ID","p2_name","p2_ID","p3_name","p3_ID",
    								"awayPlayer1","awayPlayer1_id","awayPlayer2","awayPlayer2_id","awayPlayer3","awayPlayer3_id","awayPlayer4","awayPlayer4_id","awayPlayer5","awayPlayer5_id","awayPlayer6","awayPlayer6_id",
    								"homePlayer1","homePlayer1_id","homePlayer2","homePlayer2_id","homePlayer3","homePlayer3_id","homePlayer4","homePlayer4_id","homePlayer5","homePlayer5_id","homePlayer6","homePlayer6_id",
    								"Away_Players","Home_Players","Away_Score","Home_Score",
    								"Away_Goalie","Away_Goalie_Id","Home_Goalie","Home_Goalie_Id",
    								"xC","yC","Home_Coach","Away_Coach"
    								));
    
    @Override
    protected void map(LongWritable key, Text value,
			Context context)
			throws IOException, InterruptedException {    	
    	
		ArrayList<String> r = new ArrayList<String>();
		for(String a: value.toString().split("\",")) {
			for(String b: a.split("\"")[0].split(","))
				r.add(b);
			if(a.split("\"").length > 1)
				r.add(a.split("\"")[1]);
		}
		if(r.get(1).equals(GameWinnerFields.getInstance().getGameID())) {
				word.set(r.get(14));//home
				context.write(word, new IntWritable(Integer.parseInt(r.get(48))));//48
				word.set(r.get(13));//away
				context.write(word, new IntWritable(Integer.parseInt(r.get(47))));//47
		}
	}
}
