import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

export default function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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
                    return (
                        <div key={topic} className="bg-slate-800 p-4 rounded">
                            <div className="flex justify-between mb-1">
                                <span className="font-semibold">{topic}</span>
                                <span className="text-slate-400 text-sm">{p.solved} / {p.total}</span>
                            </div>
                            <div className="bg-slate-700 h-2 rounded overflow-hidden">
                                <div className="bg-emerald-500 h-full" style={{ width: `${pct}%` }} />
                            </div>
                        </div>
                    );
                })}
                {Object.keys(topicProgress).length === 0 && (
                    <div className="text-slate-400">No topic data yet.</div>
                )}
            </div>

            <div className="mt-6 text-slate-400 text-sm">
                Total submissions: {stats.totalSubmissions}
            </div>
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
