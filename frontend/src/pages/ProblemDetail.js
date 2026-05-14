import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const DIFF_COLORS = {
    Easy: "text-emerald-400",
    Medium: "text-amber-400",
    Hard: "text-rose-400",
};

const STATUS_COLOR = {
    Accepted: "text-emerald-400",
    "Wrong Answer": "text-rose-400",
    TLE: "text-amber-400",
    "Runtime Error": "text-rose-400",
    "Compilation Error": "text-rose-400",
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
    const [tab, setTab] = useState("run"); // run | custom | submit | submissions
    const [mySubmissions, setMySubmissions] = useState(null);
    const [loadingSubs, setLoadingSubs] = useState(false);
    const [customTests, setCustomTests] = useState([{ input: "", expectedOutput: "" }]);
    const [customResults, setCustomResults] = useState(null);
    const [runningCustom, setRunningCustom] = useState(false);

    const loadMySubmissions = async () => {
        if (!user) return;
        setLoadingSubs(true);
        try {
            const { data } = await api.get(`/submissions?problemId=${id}`);
            setMySubmissions(data || []);
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setLoadingSubs(false);
        }
    };

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

    const onRunCustom = async () => {
        const cleaned = customTests
            .map((t) => ({
                input: t.input ?? "",
                expectedOutput: t.expectedOutput ?? "",
            }))
            .filter((t) => t.input.trim() !== "" || t.expectedOutput.trim() !== "");
        if (cleaned.length === 0) {
            setError("Add at least one custom test case with input.");
            setTab("custom");
            return;
        }
        setRunningCustom(true);
        setError("");
        setCustomResults(null);
        setTab("custom");
        try {
            const { data } = await api.post("/run-custom", {
                problemId: Number(id),
                code,
                language: "java",
                testCases: cleaned,
            });
            setCustomResults(data.results || []);
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setRunningCustom(false);
        }
    };

    const updateCustomTest = (idx, field, value) => {
        setCustomTests((prev) =>
            prev.map((t, i) => (i === idx ? { ...t, [field]: value } : t))
        );
    };
    const addCustomTest = () =>
        setCustomTests((prev) => [...prev, { input: "", expectedOutput: "" }]);
    const removeCustomTest = (idx) =>
        setCustomTests((prev) =>
            prev.length === 1 ? prev : prev.filter((_, i) => i !== idx)
        );

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
            loadMySubmissions();
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
                                onClick={() => setTab("custom")}
                                className={`px-3 py-1 rounded text-sm ${tab === "custom" ? "bg-slate-700" : "bg-slate-800"}`}
                            >Custom Tests</button>
                            <button
                                onClick={() => setTab("submit")}
                                className={`px-3 py-1 rounded text-sm ${tab === "submit" ? "bg-slate-700" : "bg-slate-800"}`}
                            >Submit Result</button>
                            {user && (
                                <button
                                    onClick={() => {
                                        setTab("submissions");
                                        if (mySubmissions === null) loadMySubmissions();
                                    }}
                                    className={`px-3 py-1 rounded text-sm ${tab === "submissions" ? "bg-slate-700" : "bg-slate-800"}`}
                                >My Submissions</button>
                            )}
                        </div>

                        {error && <div className="text-red-400 mb-3">{error}</div>}

                        {tab === "run" && (
                            <RunOutput running={running} results={runResults} />
                        )}
                        {tab === "custom" && (
                            <CustomTests
                                tests={customTests}
                                results={customResults}
                                running={runningCustom}
                                disabled={running || submitting}
                                onChange={updateCustomTest}
                                onAdd={addCustomTest}
                                onRemove={removeCustomTest}
                                onRun={onRunCustom}
                            />
                        )}
                        {tab === "submit" && (
                            <SubmitOutput submitting={submitting} result={submitResult} />
                        )}
                        {tab === "submissions" && (
                            <MySubmissions
                                loading={loadingSubs}
                                submissions={mySubmissions}
                                onRefresh={loadMySubmissions}
                            />
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

function MySubmissions({ loading, submissions, onRefresh }) {
    if (loading) return <div className="text-slate-400">Loading submissions...</div>;
    if (submissions === null) return <div className="text-slate-500 text-sm">Loading...</div>;
    if (submissions.length === 0) {
        return (
            <div className="text-slate-400 text-sm">
                No submissions yet for this problem.{" "}
                <button onClick={onRefresh} className="text-emerald-400 hover:underline">Refresh</button>
            </div>
        );
    }
    return (
        <div>
            <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-slate-400">{submissions.length} submission{submissions.length === 1 ? "" : "s"}</span>
                <button onClick={onRefresh} className="text-emerald-400 hover:underline text-xs">Refresh</button>
            </div>
            <table className="w-full text-sm">
                <thead className="text-slate-400 text-left">
                    <tr>
                        <th className="py-1 pr-2">Status</th>
                        <th className="py-1 pr-2">Time</th>
                        <th className="py-1 pr-2">Submitted</th>
                        <th className="py-1"></th>
                    </tr>
                </thead>
                <tbody>
                    {submissions.map((s) => (
                        <tr key={s.id} className="border-t border-slate-700">
                            <td className={`py-1 pr-2 font-semibold ${STATUS_COLOR[s.status] || "text-slate-300"}`}>
                                {s.status}
                            </td>
                            <td className="py-1 pr-2 text-slate-400">
                                {s.executionTime != null ? `${Number(s.executionTime).toFixed(3)}s` : "—"}
                            </td>
                            <td className="py-1 pr-2 text-slate-400">
                                {s.createdAt ? new Date(s.createdAt).toLocaleString() : "—"}
                            </td>
                            <td className="py-1">
                                <Link to={`/submissions/${s.id}`} className="text-emerald-400 hover:underline">View code</Link>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function CustomTests({ tests, results, running, disabled, onChange, onAdd, onRemove, onRun }) {
    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <span className="text-sm text-slate-400">
                    Add your own test cases. Expected output is optional — leave it blank to just see your program's output.
                </span>
                <div className="flex gap-2">
                    <button
                        onClick={onAdd}
                        className="bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-xs"
                    >+ Add Case</button>
                    <button
                        onClick={onRun}
                        disabled={running || disabled}
                        className="bg-emerald-600 hover:bg-emerald-700 px-3 py-1 rounded text-xs font-semibold disabled:opacity-50"
                    >{running ? "Running..." : "Run Custom"}</button>
                </div>
            </div>

            <div className="space-y-3">
                {tests.map((t, i) => (
                    <div key={i} className="p-3 rounded border border-slate-700 bg-slate-800/40">
                        <div className="flex items-center justify-between mb-2">
                            <span className="text-sm font-semibold text-slate-300">Case {i + 1}</span>
                            <button
                                onClick={() => onRemove(i)}
                                disabled={tests.length === 1}
                                className="text-xs text-rose-400 hover:underline disabled:opacity-30 disabled:no-underline"
                            >Remove</button>
                        </div>
                        <label className="text-xs text-slate-400">Input</label>
                        <textarea
                            value={t.input}
                            onChange={(e) => onChange(i, "input", e.target.value)}
                            rows={3}
                            placeholder="stdin passed to your program"
                            className="w-full mt-1 mb-2 bg-slate-900 border border-slate-700 rounded p-2 text-sm font-mono"
                        />
                        <label className="text-xs text-slate-400">Expected Output (optional)</label>
                        <textarea
                            value={t.expectedOutput}
                            onChange={(e) => onChange(i, "expectedOutput", e.target.value)}
                            rows={2}
                            placeholder="leave blank to skip comparison"
                            className="w-full mt-1 bg-slate-900 border border-slate-700 rounded p-2 text-sm font-mono"
                        />
                    </div>
                ))}
            </div>

            {results && (
                <div className="space-y-3 pt-2">
                    <div className="text-sm font-semibold text-slate-300">Results</div>
                    {results.length === 0 && (
                        <div className="text-slate-500 text-sm">No results.</div>
                    )}
                    {results.map((r, i) => (
                        <div
                            key={i}
                            className={`p-3 rounded border ${r.passed ? "border-emerald-600 bg-emerald-900/20" : "border-rose-600 bg-rose-900/20"}`}
                        >
                            <div className="flex justify-between mb-1">
                                <span className="font-semibold">Case {i + 1}</span>
                                <span className={r.passed ? "text-emerald-400" : "text-rose-400"}>
                                    {r.passed ? `✓ ${r.status}` : `✗ ${r.status}`}
                                </span>
                            </div>
                            <div className="text-xs text-slate-400">Input</div>
                            <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.input}</pre>
                            {r.expectedOutput !== "" && (
                                <>
                                    <div className="text-xs text-slate-400 mt-2">Expected</div>
                                    <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.expectedOutput}</pre>
                                </>
                            )}
                            <div className="text-xs text-slate-400 mt-2">Actual</div>
                            <pre className="text-sm bg-slate-800 p-2 rounded whitespace-pre-wrap">{r.actualOutput}</pre>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

