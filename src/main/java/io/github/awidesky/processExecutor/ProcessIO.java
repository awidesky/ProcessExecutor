package io.github.awidesky.processExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.function.Consumer;

import io.github.awidesky.guiUtil.Logger;

public class ProcessIO {
	
	private Consumer<BufferedReader> stdout = null;
	private Consumer<BufferedReader> stderr = null;
	private Consumer<BufferedWriter> stdin = null;
	
	public ProcessIO(Logger logger) {
		stdout = br -> br.lines().forEach(logger::log);
		stderr = br -> br.lines().forEach(logger::log);
	}
	public ProcessIO(Logger out, Logger err) {
		stdout = br -> br.lines().forEach(out::log);
		stderr = br -> br.lines().forEach(err::log);
	}
	public ProcessIO(Consumer<BufferedReader> stdout, Consumer<BufferedReader> stderr) {
		this.stdout = stdout;
		this.stderr = stderr;
	}
	public ProcessIO(Consumer<BufferedReader> stdout, Consumer<BufferedReader> stderr, Consumer<BufferedWriter> stdin) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.stdin = stdin;
	}
	
	public void stdout(BufferedReader br) {
		stdout.accept(br);
	}
	public void stderr(BufferedReader br) {
		stderr.accept(br);
	}
	public void stdin(BufferedWriter bw) {
		if(stdin != null) stdin.accept(bw);
	}
	
}
