package it.polimi.tracechecking.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.launcher.SparkLauncher;

import it.polimi.tracechecking.common.model.Permission;

public class TraceChecker {

	public Map<String, Boolean> checkTrace(String pathToEventsFile, List<Permission> permissions) {
		Map<String, Boolean> result = new HashMap<String, Boolean>();

		for (Permission p : permissions) {
			result.put(p.getId(), false);
		}
		return result;
	}

	public void checkTrace(String pathToEventsFile, String pathToFormulaeFile, String pathToOutputFile) {
		try {
			final String javaHome = Config.getProperty(Config.JAVA_HOME);
			final String sparkHome = Config.getProperty(Config.SPARK_HOME);
			final String appResource = Config.getProperty(Config.PATH_TO_APP); //i'd rather put it in resources idk
			final String mainClass = "it.polimi.krstic.MTLMapReduce.SparkHistoryCheck";
			//
			// parameters passed to the SparkFriendRecommendation
			final String[] appArgs = new String[] {
					// "--arg",
					pathToEventsFile, // eg
										// "hdfs://localhost:9000/user/fil/trace1"

					// "--arg",
					pathToFormulaeFile, // eg
										// "/home/filippo/Scrivania/formula1",

					// "--arg"
					pathToOutputFile// eg "/home/filippo/Scrivania/output"
			};
			//
			//
			SparkLauncher spark = new SparkLauncher().setVerbose(true).setSparkHome(sparkHome)
					.setAppResource(appResource) // "/my/app.jar"
					.setMainClass(mainClass) // "my.spark.app.Main"
					.setMaster(Config.getProperty(Config.SPARK_HOST)).setConf(SparkLauncher.DRIVER_MEMORY, "1g")
					.setConf(SparkLauncher.EXECUTOR_CORES, "1").setConf(SparkLauncher.EXECUTOR_MEMORY, "1g")
					.addAppArgs(appArgs);
			//
			// Launches a sub-process that will start the configured Spark
			// application.
			Process proc = spark.launch();

			//
			InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(proc.getInputStream(),
					"input");
			Thread inputThread = new Thread(inputStreamReaderRunnable, "LogStreamReader input");
			inputThread.start();
			//
			InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(proc.getErrorStream(),
					"error");
			Thread errorThread = new Thread(errorStreamReaderRunnable, "LogStreamReader error");
			errorThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
