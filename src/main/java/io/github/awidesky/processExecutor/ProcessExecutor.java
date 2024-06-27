package io.github.awidesky.processExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import io.github.awidesky.guiUtil.Logger;
import io.github.awidesky.guiUtil.SwingDialogs;


/**
 * Executes third party program & commands
 * */
public class ProcessExecutor {
	
	private static final Charset NATIVECHARSET = Charset.forName(System.getProperty("native.encoding"));;
	private static ExecutorService executorService = null;
	
	public static void setThreadPool(ExecutorService threadPool) { executorService = threadPool; }
	private static Future<?> submit(Runnable r) {
		if(executorService == null) executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		return executorService.submit(r);
	}
	
	public static int runNow(Logger logger, File dir, String... command) throws InterruptedException, ExecutionException, IOException {
		return run(Arrays.asList(command), dir, new ProcessIO(logger)).wait_all();
	}
	public static ProcessHandle run(List<String> command, File dir, ProcessIO io) throws IOException {
		
		ProcessBuilder pb = new ProcessBuilder(command);
		// start process
		Process p = pb.directory(dir).start();
		
		Future<?> f1 = submit(() -> {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), NATIVECHARSET))) {
				io.stdout(br);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "Process : " + command.stream().collect(Collectors.joining(" "))
						+ "\n%e%", e, false);
			}
		});
		Future<?> f2 = submit(() -> {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), NATIVECHARSET))) {
				io.stderr(br);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "Process : " + command.stream().collect(Collectors.joining(" "))
						+ "\n%e%", e, false);
			}
		});
		Future<?> f3 = submit(() -> {
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), NATIVECHARSET))) {
				io.stdin(bw);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "Process : " + command.stream().collect(Collectors.joining(" "))
						+ "\n%e%", e, false);
			}
		});
		
		return new ProcessHandle(p, f1, f2, f3); 
	}
	
	public static class ProcessHandle {
		private final Process proc;
		private final Future<?> stdout;
		private final Future<?> stderr;
		private final Future<?> stdin;
		
		public ProcessHandle(Process proc, Future<?> stdout, Future<?> stderr, Future<?> stdin) {
			this.proc = proc;
			this.stdout = stdout;
			this.stderr = stderr;
			this.stdin = stdin;
		}
		
		public int waitProcess() throws InterruptedException { return proc.waitFor(); }
		public void wait_output() throws InterruptedException, ExecutionException { stdout.get(); stderr.get(); }
		public void wait_input() throws InterruptedException, ExecutionException { stdin.get(); }
		
		public int wait_all() throws InterruptedException, ExecutionException { wait_input(); wait_output(); return waitProcess(); }
		
		public Process getProcess() { return proc; }
	}
}
