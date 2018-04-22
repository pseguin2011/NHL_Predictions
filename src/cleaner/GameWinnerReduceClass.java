package cleaner;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class GameWinnerReduceClass extends Reducer<Text, IntWritable, Text, IntWritable>{

	protected void reduce(Text key, Iterable<IntWritable> values,
			Context context)
			throws IOException, InterruptedException {
		
		
			int max=0;
			for(IntWritable x: values)
			{
				max=Math.max(x.get(),max);
			}
			context.write(key, new IntWritable(max));
	}	
}
