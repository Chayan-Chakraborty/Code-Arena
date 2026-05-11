import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const DIFF_COLORS = {
    Easy: "text-emerald-400",
    Medium: "text-amber-400",
    Hard: "text-rose-400",
};

export default function ProblemDetail() {
    const { id } = useParams();
    const { user } = useAuth();
    const [problem, setProblem] = useState(null);
    const [code, setCode] = useState("");
    const [lastRunCode, setLastRunCode] = useState("");
    const [loading, setLoading] = useState(true);
    const [running, setRunning] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [runResults, setRunResults] = useState(null);
    const [submitResult, setSubmitResult] = useState(null);
    const [error, setError] = useState("");
    const [tab, setTab] = useState("run"); // run | submit

    useEffect(() => {
        setLoading(true);
        api
            .get(`/problems/${id}`)
            .then((r) => {
                setProblem(r.data);
                setCode(r.data.starterCode || "");
                setLastRunCode("");
            })
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, [id]);

    const onRun = async () => {
        setRunning(true);
        setError("");
        setRunResults(null);
        setTab("run");
        try {
            const { data } = await api.post("/run", {
                problemId: Number(id),
                code,
                language: "java",
            });
            setRunResults(data.results || []);
            setLastRunCode(code);
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setRunning(false);
        }
    };

    const hasPassingRun =
        Array.isArray(runResults) &&
        runResults.length > 0 &&
        runResults.every((result) => result.passed) &&
        lastRunCode === code;

    const onSubmit = async () => {
        if (!user) {
            setError("Please login to submit.");
            return;
        }
        if (!hasPassingRun) {
            setError("Run the current code and pass all sample test cases before submitting.");
            setTab("run");
            return;
        }
        setSubmitting(true);
        setError("");
        setSubmitResult(null);
        setTab("submit");
        try {
            const { data } = await api.post("/submit", {
                problemId: Number(id),
                code,
                language: "java",
            });
            setSubmitResult(data);
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="p-8 text-slate-400">Loading problem...</div>;
    if (!problem) return <div className="p-8 text-red-400">Problem not found</div>;

    return (
        <div className="flex h-[calc(100vh-57px)]">
            {/* Left: description */}
            <div className="w-1/2 overflow-y-auto p-6 border-r border-slate-700">
                <div className="flex items-center gap-3 mb-2">
                    <h1 className="text-2xl font-bold">{problem.id}. {problem.title}</h1>
                    <span className={`text-sm font-semibold ${DIFF_COLORS[problem.difficulty] || ""}`}>
                        {problem.difficulty}
                    </span>
                    <span className="text-xs bg-slate-700 px-2 py-1 rounded">{problem.topic}</span>
                </div>

                <div className="prose prose-invert max-w-none text-slate-300 whitespace-pre-wrap mb-4">
                    {problem.description}
                </div>

                <h3 className="font-semibold mt-4 mb-2 text-emerald-400">Constraints</h3>
                <pre className="bg-slate-800 p-3 rounded text-sm whitespace-pre-wrap">{problem.constraints}</pre>

                <h3 className="font-semibold mt-4 mb-2 text-emerald-400">Sample Input</h3>
                <pre className="bg-slate-800 p-3 rounded text-sm whitespace-pre-wrap">{problem.sampleInput}</pre>

                <h3 className="font-semibold mt-4 mb-2 text-emerald-400">Sample Output</h3>
                <pre className="bg-slate-800 p-3 rounded text-sm whitespace-pre-wrap">{problem.sampleOutput}</pre>
            </div>

            {/* Right: editor + results */}
            <div className="w-1/2 flex flex-col">
                <div className="flex items-center justify-between p-3 bg-slate-800 border-b border-slate-700">
                    <span className="text-sm text-slate-400">Java</span>
                    <div className="flex gap-2">
                        <button
                            onClick={onRun}
                            disabled={running || submitting}
                            className="bg-slate-600 hover:bg-slate-500 px-4 py-1.5 rounded text-sm disabled:opacity-50"
                        >
                            {running ? "Running..." : "Run"}
                        </button>
                        <button
                            onClick={onSubmit}
                            disabled={running || submitting || !hasPassingRun}
                            className="bg-emerald-600 hover:bg-emerald-700 px-4 py-1.5 rounded text-sm font-semibold disabled:opacity-50"
                            title={hasPassingRun ? "Submit solution" : "Pass all sample test cases with the current code to submit"}
                        >
                            {submitting ? "Submitting..." : "Submit"}
                        </button>
                    </div>
                </div>

                <div className="flex-1 min-h-0">
                    <Editor
                        height="60%"
                        language="java"
                        theme="vs-dark"
                        value={code}
                        onChange={(v) => {
                            setCode(v ?? "");
                            setSubmitResult(null);
                        }}
                        options={{ fontSize: 14, minimap: { enabled: false }, automaticLayout: true }}
                    />

                    <div className="h-[40%] overflow-y-auto bg-slate-900 border-t border-slate-700 p-4">
                        <div className="flex gap-2 mb-3">
                            <button
                                onClick={() => setTab("run")}
                                className={`px-3 py-1 rounded text-sm ${tab === "run" ? "bg-slate-700" : "bg-slate-800"}`}
                            >Run Output</button>
                            <button
                                onClick={() => setTab("submit")}
                                className={`px-3 py-1 rounded text-sm ${tab === "submit" ? "bg-slate-700" : "bg-slate-800"}`}
                            >Submit Result</button>
                        </div>

                        {error && <div className="text-red-400 mb-3">{error}</div>}

                        {tab === "run" && (
                            <RunOutput running={running} results={runResults} />
                        )}
                        {tab === "submit" && (
                            <SubmitOutput submitting={submitting} result={submitResult} />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

function RunOutput({ running, results }) {
    if (running) return <div className="text-slate-400">Executing on sample test cases...</div>;
    if (!results) return <div className="text-slate-500 text-sm">Click "Run" to test against sample cases.</div>;
    if (results.length === 0) return <div className="text-slate-400">No sample test cases.</div>;
    return (
        <div className="space-y-3">
            {results.map((r, i) => (
                <div
                    key={i}
                    className={`p-3 rounded border ${r.passed ? "border-emerald-600 bg-emerald-900/20" : "border-rose-600 bg-rose-900/20"}`}
                >
                    <div className="flex justify-between mb-1">
                        <span className="font-semibold">Test Case {i + 1}</span>
                        <span className={r.passed ? "text-emerald-400" : "text-rose-400"}>
                            {r.passed ? "✓ Passed" : `✗ ${r.status}`}
                        </span>
                    </div>
                    <div className="text-xs text-slate-400">Input</div>
                    <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.input}</pre>
                    <div className="text-xs text-slate-400 mt-2">Expected</div>
                    <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.expectedOutput}</pre>
                    <div className="text-xs text-slate-400 mt-2">Actual</div>
                    <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.actualOutput}</pre>
                </div>
            ))}
        </div>
    );
}

function SubmitOutput({ submitting, result }) {
    if (submitting) return <div className="text-slate-400">Running hidden test cases...</div>;
    if (!result) return <div className="text-slate-500 text-sm">Click "Submit" to evaluate against hidden tests.</div>;
    const accepted = result.status === "Accepted";
    return (
        <div>
            <div className={`text-2xl font-bold mb-2 ${accepted ? "text-emerald-400" : "text-rose-400"}`}>
                {result.status}
            </div>
            <div className="text-sm text-slate-300 mb-3">
                Passed {result.passed} / {result.total} test cases
                {result.executionTime != null && <> · {Number(result.executionTime).toFixed(3)}s</>}
            </div>
            {!accepted && result.results && (() => {
                const failed = result.results.filter((r) => !r.passed);
                return (
                    <div>
                        <div className="text-sm font-semibold text-rose-400 mb-2">
                            {failed.length} failed test case{failed.length === 1 ? "" : "s"}
                        </div>
                        <div className="space-y-2">
                            {failed.map((r, i) => {
                                const idx = result.results.indexOf(r);
                                return (
                                    <div key={i} className="p-3 rounded bg-rose-900/20 border border-rose-700 text-sm">
                                        <div className="flex justify-between mb-1">
                                            <span className="font-semibold text-rose-400">Test Case {idx + 1}</span>
                                            <span className="text-rose-300">{r.status}</span>
                                        </div>
                                        <div className="text-xs text-slate-400">Input</div>
                                        <pre className="bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.input}</pre>
                                        <div className="text-xs text-slate-400 mt-1">Expected</div>
                                        <pre className="bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.expectedOutput}</pre>
                                        <div className="text-xs text-slate-400 mt-1">Actual</div>
                                        <pre className="bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.actualOutput}</pre>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                );
            })()}
        </div>
    );
}
