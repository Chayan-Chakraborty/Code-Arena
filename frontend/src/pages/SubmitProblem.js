import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const DIFFICULTIES = ["Easy", "Medium", "Hard"];

export default function SubmitProblem() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [form, setForm] = useState({
        title: "",
        description: "",
        difficulty: "Easy",
        topic: "",
        constraints: "",
        sampleInput: "",
        sampleOutput: "",
        starterCode: "",
    });
    const [tests, setTests] = useState([{ input: "", expectedOutput: "", isSample: true }]);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!user) navigate("/login");
    }, [user, navigate]);

    const setField = (k, v) => setForm((p) => ({ ...p, [k]: v }));

    const addTest = () => setTests((p) => [...p, { input: "", expectedOutput: "", isSample: false }]);
    const removeTest = (i) => setTests((p) => (p.length === 1 ? p : p.filter((_, idx) => idx !== i)));
    const updateTest = (i, k, v) => setTests((p) => p.map((t, idx) => (idx === i ? { ...t, [k]: v } : t)));

    const submit = async (e) => {
        e.preventDefault();
        setError("");
        if (!form.title.trim()) {
            setError("Title is required.");
            return;
        }
        const validTests = tests
            .map((t) => ({
                input: t.input ?? "",
                expectedOutput: t.expectedOutput ?? "",
                isSample: !!t.isSample,
            }))
            .filter((t) => t.input.trim() !== "" || t.expectedOutput.trim() !== "");
        if (validTests.length === 0) {
            setError("Add at least one test case.");
            return;
        }
        setSubmitting(true);
        try {
            const { data: problem } = await api.post("/problems", form);
            // Add test cases sequentially
            for (const t of validTests) {
                await api.post(`/problems/${problem.id}/testcases`, t);
            }
            navigate(`/problems/${problem.id}`);
        } catch (err) {
            setError(err.response?.data?.error || err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const isAdmin = user?.role === "ADMIN";

    return (
        <div className="max-w-3xl mx-auto p-8">
            <h1 className="text-3xl font-bold mb-2">Submit a Problem</h1>
            <p className="text-slate-400 mb-6">
                {isAdmin
                    ? "As an admin, your problem will be approved immediately."
                    : "Your problem will be submitted for admin review. You can edit its test cases while it's pending."}
            </p>
            <form onSubmit={submit} className="space-y-4">
                <div>
                    <label className="text-sm text-slate-400">Title *</label>
                    <input
                        value={form.title}
                        onChange={(e) => setField("title", e.target.value)}
                        className="w-full p-2 rounded bg-slate-800 border border-slate-700"
                    />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="text-sm text-slate-400">Difficulty</label>
                        <select
                            value={form.difficulty}
                            onChange={(e) => setField("difficulty", e.target.value)}
                            className="w-full p-2 rounded bg-slate-800 border border-slate-700"
                        >
                            {DIFFICULTIES.map((d) => (
                                <option key={d} value={d}>{d}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="text-sm text-slate-400">Topic</label>
                        <input
                            value={form.topic}
                            onChange={(e) => setField("topic", e.target.value)}
                            placeholder="Arrays, Strings, DP, ..."
                            className="w-full p-2 rounded bg-slate-800 border border-slate-700"
                        />
                    </div>
                </div>
                <div>
                    <label className="text-sm text-slate-400">Description</label>
                    <textarea
                        rows={6}
                        value={form.description}
                        onChange={(e) => setField("description", e.target.value)}
                        className="w-full p-2 rounded bg-slate-800 border border-slate-700 font-mono text-sm"
                    />
                </div>
                <div>
                    <label className="text-sm text-slate-400">Constraints</label>
                    <textarea
                        rows={3}
                        value={form.constraints}
                        onChange={(e) => setField("constraints", e.target.value)}
                        className="w-full p-2 rounded bg-slate-800 border border-slate-700 font-mono text-sm"
                    />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="text-sm text-slate-400">Sample Input</label>
                        <textarea
                            rows={3}
                            value={form.sampleInput}
                            onChange={(e) => setField("sampleInput", e.target.value)}
                            className="w-full p-2 rounded bg-slate-800 border border-slate-700 font-mono text-sm"
                        />
                    </div>
                    <div>
                        <label className="text-sm text-slate-400">Sample Output</label>
                        <textarea
                            rows={3}
                            value={form.sampleOutput}
                            onChange={(e) => setField("sampleOutput", e.target.value)}
                            className="w-full p-2 rounded bg-slate-800 border border-slate-700 font-mono text-sm"
                        />
                    </div>
                </div>
                <div>
                    <label className="text-sm text-slate-400">Starter Code</label>
                    <textarea
                        rows={6}
                        value={form.starterCode}
                        onChange={(e) => setField("starterCode", e.target.value)}
                        placeholder="// Java starter template"
                        className="w-full p-2 rounded bg-slate-800 border border-slate-700 font-mono text-sm"
                    />
                </div>

                <div className="border-t border-slate-700 pt-4">
                    <div className="flex justify-between items-center mb-3">
                        <h2 className="text-xl font-semibold">Test Cases</h2>
                        <button
                            type="button"
                            onClick={addTest}
                            className="bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-sm"
                        >+ Add Test Case</button>
                    </div>
                    <div className="space-y-3">
                        {tests.map((t, i) => (
                            <div key={i} className="p-3 rounded border border-slate-700 bg-slate-800/40 space-y-2">
                                <div className="flex justify-between items-center">
                                    <span className="font-semibold text-sm">Case {i + 1}</span>
                                    <button
                                        type="button"
                                        onClick={() => removeTest(i)}
                                        disabled={tests.length === 1}
                                        className="text-xs text-rose-400 hover:underline disabled:opacity-30"
                                    >Remove</button>
                                </div>
                                <label className="text-xs text-slate-400">Input</label>
                                <textarea
                                    value={t.input}
                                    onChange={(e) => updateTest(i, "input", e.target.value)}
                                    rows={2}
                                    className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-sm font-mono"
                                />
                                <label className="text-xs text-slate-400">Expected Output</label>
                                <textarea
                                    value={t.expectedOutput}
                                    onChange={(e) => updateTest(i, "expectedOutput", e.target.value)}
                                    rows={2}
                                    className="w-full p-2 bg-slate-900 border border-slate-700 rounded text-sm font-mono"
                                />
                                <label className="flex items-center gap-2 text-xs text-slate-300">
                                    <input
                                        type="checkbox"
                                        checked={!!t.isSample}
                                        onChange={(e) => updateTest(i, "isSample", e.target.checked)}
                                    />
                                    Sample (visible to solvers)
                                </label>
                            </div>
                        ))}
                    </div>
                </div>

                {error && <div className="text-red-400">{error}</div>}

                <div className="flex justify-end gap-2">
                    <button
                        type="button"
                        onClick={() => navigate("/problems")}
                        className="bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded"
                    >Cancel</button>
                    <button
                        type="submit"
                        disabled={submitting}
                        className="bg-emerald-600 hover:bg-emerald-700 px-4 py-2 rounded font-semibold disabled:opacity-50"
                    >{submitting ? "Submitting..." : "Submit Problem"}</button>
                </div>
            </form>
        </div>
    );
}
