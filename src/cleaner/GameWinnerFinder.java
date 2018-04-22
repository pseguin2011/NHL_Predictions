package cleaner;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;



public class GameWinnerFinder extends Configured implements Tool{

	
	public static void main(String[] args) throws Exception{
		GameWinnerFields.getInstance().setGameID("30417");
		String[] args2 = {"/home/pseguin/Desktop/Data/nhl_pbp20082009.csv", "/home/pseguin/Desktop/Data/GameWinner"};	
		int exitCode = ToolRunner.run(new GameWinnerFinder(), args2);
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/pseguin/Desktop/Data/Parsed Game Data/games.txt"));
		long time = System.currentTimeMillis()+ 10000;
		while(System.currentTimeMillis() < time){}
		writer.close();
		System.exit(exitCode);		
	}
 
	public int run(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.printf("Usage: %s needs two arguments, input and output files\n", getClass().getSimpleName());
			return -1;
		}
		Job job = new Job();
		job.setJarByClass(GameWinnerFinder.class);
		job.setJobName("WordCounter");
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setMapperClass(GameWinnerMapClass.class);
//		job.setReducerClass(IntSumReducer.class);
		job.setReducerClass(GameWinnerReduceClass.class);
	    TextInputFormat.addInputPath(job,new Path(args[0]));
	   
	    FileSystem.get(getConf()).delete(new Path(args[1]),true);
	    TextOutputFormat.setOutputPath(job, new Path(args[1]));
		int returnValue = job.waitForCompletion(true) ? 0:1;
		if(job.isSuccessful()) {
			System.out.println("Job was successful");
		} else if(!job.isSuccessful()) {
			System.out.println("Job was not successful");			
		}
		return returnValue;
	}
}
