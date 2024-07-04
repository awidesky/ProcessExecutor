package io.github.awidesky.processExecutor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.awidesky.guiUtil.Logger;


/**
 * Executes third party program and commands
 * */
public class ProcessExecutor {
	
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
		
		Future<?> f1 = submit(() -> io.stdout(p.getInputStream()));
		Future<?> f2 = submit(() -> io.stderr(p.getErrorStream()));
		Future<?> f3 = submit(() -> io.stdin(p.getOutputStream()));
		
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
