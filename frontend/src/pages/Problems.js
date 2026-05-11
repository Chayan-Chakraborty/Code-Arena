import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import api from "../api/axios";

const DIFF_COLORS = {
    Easy: "text-emerald-400",
    Medium: "text-amber-400",
    Hard: "text-rose-400",
};

export default function Problems() {
    const [params, setParams] = useSearchParams();
    const topic = params.get("topic");
    const [problems, setProblems] = useState([]);
    const [allTopics, setAllTopics] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        api.get("/problems")
            .then((r) => {
                const topics = [...new Set(r.data.map((problem) => problem.topic))].sort((a, b) => a.localeCompare(b));
                setAllTopics(topics);
            })
            .catch(() => {});
    }, []);

    useEffect(() => {
        setLoading(true);
        setError("");
        api
            .get("/problems", { params: topic ? { topic } : {} })
            .then((r) => setProblems(r.data))
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, [topic]);

    return (
        <div className="p-8 max-w-5xl mx-auto">
            <h1 className="text-3xl font-bold mb-2">
                Problems {topic && <span className="text-emerald-400">/ {topic}</span>}
            </h1>
            <p className="text-slate-400 mb-6">Pick a problem and start coding.</p>

            {allTopics.length > 0 && (
                <div className="flex flex-wrap gap-2 mb-6">
                    <button
                        onClick={() => setParams({})}
                        className={`px-3 py-1.5 rounded-full text-sm border ${!topic ? "bg-emerald-600 border-emerald-600" : "bg-slate-800 border-slate-700"}`}
                    >
                        All Topics
                    </button>
                    {allTopics.map((name) => (
                        <button
                            key={name}
                            onClick={() => setParams({ topic: name })}
                            className={`px-3 py-1.5 rounded-full text-sm border ${topic === name ? "bg-emerald-600 border-emerald-600" : "bg-slate-800 border-slate-700"}`}
                        >
                            {name}
                        </button>
                    ))}
                </div>
            )}

            {loading && <div className="text-slate-400">Loading...</div>}
            {error && <div className="text-red-400">{error}</div>}

            <div className="bg-slate-800 rounded-lg overflow-hidden">
                <table className="w-full text-left">
                    <thead className="bg-slate-700">
                        <tr>
                            <th className="p-3">#</th>
                            <th className="p-3">Title</th>
                            <th className="p-3">Topic</th>
                            <th className="p-3">Difficulty</th>
                        </tr>
                    </thead>
                    <tbody>
                        {problems.map((p) => (
                            <tr key={p.id} className="border-t border-slate-700 hover:bg-slate-700/50">
                                <td className="p-3 text-slate-400">{p.id}</td>
                                <td className="p-3">
                                    <Link to={`/problems/${p.id}`} className="text-emerald-400 hover:underline">
                                        {p.title}
                                    </Link>
                                </td>
                                <td className="p-3 text-slate-300">{p.topic}</td>
                                <td className={`p-3 font-semibold ${DIFF_COLORS[p.difficulty] || ""}`}>
                                    {p.difficulty}
                                </td>
                            </tr>
                        ))}
                        {!loading && problems.length === 0 && (
                            <tr><td colSpan="4" className="p-6 text-center text-slate-400">No problems found.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
