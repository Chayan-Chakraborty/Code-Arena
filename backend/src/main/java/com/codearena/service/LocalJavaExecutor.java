package com.codearena.service;

import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Compiles and runs untrusted Java source locally using the JDK's javac and
 * java tools.
 *
 * Limitations: this is a development-grade sandbox. It enforces only a
 * wall-clock timeout.
 * Do NOT expose this service to the public internet without a real sandbox
 * (Docker, nsjail, etc.).
 */
@Service
public class LocalJavaExecutor {

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final String MAIN_CLASS = "Main";

    public ExecutionResult execute(String source, String stdin) {
        return execute(source, stdin, DEFAULT_TIMEOUT_MS);
    }

    public ExecutionResult execute(String source, String stdin, int timeoutMs) {
        Path workDir;
        try {
            workDir = Files.createTempDirectory("codearena-");
        } catch (IOException e) {
            return ExecutionResult.error("Could not create temp dir: " + e.getMessage());
        }

        try {
            Path srcFile = workDir.resolve(MAIN_CLASS + ".java");
            Files.writeString(srcFile, source, StandardCharsets.UTF_8);

            // ---- Compile ----
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return ExecutionResult.error("No Java compiler available. Run the server on a JDK (not a JRE).");
            }
            ByteArrayOutputStream compileErr = new ByteArrayOutputStream();
            int rc = compiler.run(null, null, compileErr,
                    "-d", workDir.toString(),
                    srcFile.toString());
            if (rc != 0) {
                return new ExecutionResult("Compilation Error", "",
                        compileErr.toString(StandardCharsets.UTF_8), 0.0);
            }

            // ---- Run ----
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-Xss16m", "-Xmx256m", "-cp", workDir.toString(), MAIN_CLASS);
            pb.redirectErrorStream(false);
            long start = System.nanoTime();
            Process proc = pb.start();

            // Feed stdin
            try (OutputStream os = proc.getOutputStream()) {
                if (stdin != null)
                    os.write(stdin.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }

            boolean finished = proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;

            if (!finished) {
                proc.destroyForcibly();
                return new ExecutionResult("Time Limit Exceeded", "", "Execution exceeded " + timeoutMs + "ms",
                        elapsed);
            }

            String stdout = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exit = proc.exitValue();
            if (exit != 0) {
                String err = stderr.isBlank() ? ("Exited with code " + exit) : stderr;
                return new ExecutionResult("Runtime Error", stdout, err, elapsed);
            }
            return new ExecutionResult("Accepted", stdout, null, elapsed);

        } catch (Exception e) {
            return ExecutionResult.error("Execution failed: " + e.getMessage());
        } finally {
            cleanup(workDir);
        }
    }

    private void cleanup(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static class ExecutionResult {
        public final String status; // Accepted / Compilation Error / Runtime Error / Time Limit Exceeded / Error
        public final String stdout;
        public final String error;
        public final double time;

        public ExecutionResult(String status, String stdout, String error, double time) {
            this.status = status;
            this.stdout = stdout;
            this.error = error;
            this.time = time;
        }

        public static ExecutionResult error(String msg) {
            return new ExecutionResult("Error", "", msg, 0.0);
        }
    }
}
