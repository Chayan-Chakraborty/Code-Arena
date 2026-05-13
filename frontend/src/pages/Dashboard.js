import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

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

export default function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [expanded, setExpanded] = useState({});

    useEffect(() => {
        if (!user) { setLoading(false); return; }
        api.get("/dashboard")
            .then((r) => setStats(r.data))
            .catch((e) => setError(e.response?.data?.error || e.message))
            .finally(() => setLoading(false));
    }, [user]);

    if (!user) {
        return (
            <div className="p-10 text-center">
                <p className="text-slate-400 mb-4">Login to view your dashboard.</p>
                <Link to="/login" className="bg-emerald-600 px-4 py-2 rounded">Login</Link>
            </div>
        );
    }

    if (loading) return <div className="p-10 text-slate-400">Loading dashboard...</div>;
    if (error) return <div className="p-10 text-red-400">{error}</div>;
    if (!stats) return null;

    const diff = stats.byDifficulty || {};
    const topicProgress = stats.topicProgress || {};
    const solvedByTopic = stats.solvedByTopic || {};
    const recent = stats.recentSubmissions || [];

    const toggle = (topic) =>
        setExpanded((prev) => ({ ...prev, [topic]: !prev[topic] }));

    return (
        <div className="p-8 max-w-5xl mx-auto">
            <h1 className="text-3xl font-bold mb-6">Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
                <Stat label="Total Solved" value={`${stats.totalSolved} / ${stats.totalProblems}`} color="text-emerald-400" />
                <Stat label="Easy" value={diff.Easy || 0} color="text-emerald-400" />
                <Stat label="Medium" value={diff.Medium || 0} color="text-amber-400" />
                <Stat label="Hard" value={diff.Hard || 0} color="text-rose-400" />
            </div>

            <h2 className="text-xl font-semibold mb-4">Topic-wise Progress</h2>
            <div className="space-y-3">
                {Object.entries(topicProgress).map(([topic, p]) => {
                    const pct = p.total ? Math.round((p.solved / p.total) * 100) : 0;
                    const solvedList = solvedByTopic[topic] || [];
                    const isOpen = !!expanded[topic];
                    return (
                        <div key={topic} className="bg-slate-800 rounded">
                            <button
                                type="button"
                                onClick={() => toggle(topic)}
                                disabled={solvedList.length === 0}
                                className="w-full text-left p-4 disabled:cursor-default"
                            >
                                <div className="flex justify-between mb-1 items-center">
                                    <span className="font-semibold flex items-center gap-2">
                                        {solvedList.length > 0 && (
                                            <span className="text-slate-400 text-xs">{isOpen ? "▼" : "▶"}</span>
                                        )}
                                        {topic}
                                    </span>
                                    <span className="text-slate-400 text-sm">{p.solved} / {p.total}</span>
                                </div>
                                <div className="bg-slate-700 h-2 rounded overflow-hidden">
                                    <div className="bg-emerald-500 h-full" style={{ width: `${pct}%` }} />
                                </div>
                            </button>
                            {isOpen && solvedList.length > 0 && (
                                <ul className="px-4 pb-4 space-y-1 text-sm border-t border-slate-700 pt-3">
                                    {solvedList.map((q) => (
                                        <li key={q.id} className="flex justify-between items-center">
                                            <Link
                                                to={`/problems/${q.id}`}
                                                className="text-emerald-400 hover:underline"
                                            >
                                                {q.id}. {q.title}
                                            </Link>
                                            <div className="flex items-center gap-3">
                                                <span className={DIFF_COLORS[q.difficulty] || "text-slate-400"}>
                                                    {q.difficulty}
                                                </span>
                                                <Link
                                                    to={`/submissions?problemId=${q.id}`}
                                                    className="text-slate-400 hover:text-emerald-400 text-xs"
                                                >
                                                    submissions →
                                                </Link>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    );
                })}
                {Object.keys(topicProgress).length === 0 && (
                    <div className="text-slate-400">No topic data yet.</div>
                )}
            </div>

            <div className="flex justify-between items-center mt-10 mb-4">
                <h2 className="text-xl font-semibold">Recent Submissions</h2>
                <Link to="/submissions" className="text-emerald-400 hover:underline text-sm">
                    View all ({stats.totalSubmissions}) →
                </Link>
            </div>

            {recent.length === 0 ? (
                <div className="text-slate-400">No submissions yet.</div>
            ) : (
                <div className="overflow-x-auto border border-slate-700 rounded">
                    <table className="w-full text-sm">
                        <thead className="bg-slate-800 text-slate-300">
                            <tr>
                                <th className="text-left px-4 py-2">Problem</th>
                                <th className="text-left px-4 py-2">Status</th>
                                <th className="text-left px-4 py-2">Time</th>
                                <th className="text-left px-4 py-2">Submitted</th>
                                <th className="text-left px-4 py-2"></th>
                            </tr>
                        </thead>
                        <tbody>
                            {recent.map((s) => (
                                <tr key={s.id} className="border-t border-slate-700 hover:bg-slate-800/40">
                                    <td className="px-4 py-2">
                                        <Link to={`/problems/${s.problemId}`} className="text-emerald-400 hover:underline">
                                            {s.problemId}. {s.problemTitle || `#${s.problemId}`}
                                        </Link>
                                        {s.difficulty && (
                                            <span className={`ml-2 text-xs ${DIFF_COLORS[s.difficulty] || "text-slate-400"}`}>
                                                {s.difficulty}
                                            </span>
                                        )}
                                    </td>
                                    <td className={`px-4 py-2 font-semibold ${STATUS_COLOR[s.status] || "text-slate-300"}`}>
                                        {s.status}
                                    </td>
                                    <td className="px-4 py-2 text-slate-400">
                                        {s.executionTime != null ? `${Number(s.executionTime).toFixed(3)}s` : "—"}
                                    </td>
                                    <td className="px-4 py-2 text-slate-400">
                                        {s.createdAt ? new Date(s.createdAt).toLocaleString() : "—"}
                                    </td>
                                    <td className="px-4 py-2">
                                        <Link to={`/submissions/${s.id}`} className="text-emerald-400 hover:underline">
                                            View code
                                        </Link>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

function Stat({ label, value, color }) {
    return (
        <div className="bg-slate-800 p-4 rounded">
            <div className="text-slate-400 text-sm">{label}</div>
            <div className={`text-3xl font-bold mt-1 ${color}`}>{value}</div>
        </div>
    );
}
