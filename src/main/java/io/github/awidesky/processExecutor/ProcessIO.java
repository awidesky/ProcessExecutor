package io.github.awidesky.processExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.awidesky.guiUtil.Logger;
import io.github.awidesky.guiUtil.SwingDialogs;

public class ProcessIO {
	private static final Charset NATIVECHARSET = Charset.forName(System.getProperty("native.encoding"));
	
	private Consumer<InputStream> stdout = null;
	private Consumer<InputStream> stderr = null;
	private Consumer<OutputStream> stdin = null;
	
	public ProcessIO() {}
	public ProcessIO(Logger logger) {
		setStdout(logger);
		setStderr(logger);
	}
	public ProcessIO(Logger out, Logger err) {
		setStdout(out);
		setStderr(err);
	}
	public ProcessIO(Logger logger, Consumer<BufferedWriter> stdin) {
		this(logger);
		setBufferedStdin(stdin);
	}
	
	public ProcessIO(Consumer<BufferedReader> stdout, Consumer<BufferedReader> stderr) {
		setBufferedStdout(stdout);
		setBufferedStderr(stderr);
	}
	public ProcessIO(Consumer<BufferedReader> stdout, Consumer<BufferedReader> stderr, Consumer<BufferedWriter> stdin) {
		this(stdout, stderr);
		setBufferedStdin(stdin);
	}
	
	public ProcessIO stdout(InputStream is) {
		stdout.accept(is);
		return this;
	}
	public ProcessIO stderr(InputStream is) {
		stderr.accept(is);
		return this;
	}
	public ProcessIO stdin(OutputStream os) {
		if(stdin != null) stdin.accept(os);
		return this;
	}
	
	public ProcessIO setStdout(Consumer<InputStream> stdout) {
		this.stdout = stdout;
		return this;
	}
	public ProcessIO setStderr(Consumer<InputStream> stderr) {
		this.stderr = stderr;
		return this;
	}
	public ProcessIO setStdin(Consumer<OutputStream> stdin) {
		this.stdin = stdin;
		return this;
	}
	/**
	 * If an Exception is throw, shows the error via swingDialogs.
	 */
	public ProcessIO setBufferedStdout(Consumer<BufferedReader> stdout) {
		this.stdout = is -> {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, NATIVECHARSET))) {
				stdout.accept(br);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "%e%", e, false);
			}
		};
		return this;
	}
	/**
	 * If an Exception is throw, shows the error via swingDialogs.
	 */
	public ProcessIO setBufferedStderr(Consumer<BufferedReader> stderr) {
		this.stderr = is -> {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, NATIVECHARSET))) {
				stderr.accept(br);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "%e%", e, false);
			}
		};
		return this;
	}
	/**
	 * If an Exception is throw, shows the error via swingDialogs.
	 */
	public ProcessIO setBufferedStdin(Consumer<BufferedWriter> stdin) {
		this.stdin = is -> {
			try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(is, NATIVECHARSET))) {
				stdin.accept(br);
			} catch (IOException e) {
				SwingDialogs.error("Unable to close process input stream!", "%e%", e, false);
			}
		};
		return this;
	}
	/**
	 * Does not show or throw any error. 
	 */
	public ProcessIO setPrintStdout(Consumer<Scanner> stdout) {
		this.stdout = is -> {
			stdout.accept(new Scanner(new InputStreamReader(is, NATIVECHARSET)));
		};
		return this;
	}
	/**
	 * Does not show or throw any error. 
	 */
	public ProcessIO setPrintStderr(Consumer<Scanner> stderr) {
		this.stderr = is -> {
			stderr.accept(new Scanner(new InputStreamReader(is, NATIVECHARSET)));
		};
		return this;
	}
	/**
	 * Does not show or throw any error. 
	 */
	public ProcessIO setPrintStdin(Consumer<PrintWriter> stdin) {
		this.stdin = is -> {
			stdin.accept(new PrintWriter(new OutputStreamWriter(is, NATIVECHARSET)));
		};
		return this;
	}
	public ProcessIO setStdout(Logger logger) {
		setBufferedStdout(br -> br.lines().forEach(logger::log));
		return this;
	}
	public ProcessIO setStderr(Logger logger) {
		setBufferedStderr(br -> br.lines().forEach(logger::log));
		return this;
	}
	/**
	 * input stream will terminate when null is supplied. 
	 */
	public ProcessIO setStdin(Supplier<String> s) {
		setPrintStdin(pw -> {
			Stream.generate(s::get).takeWhile(Objects::nonNull).forEach(pw::println);
		});
		return this;
	}
}
