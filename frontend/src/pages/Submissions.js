import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const STATUS_COLOR = {
    Accepted: "text-emerald-400",
    "Wrong Answer": "text-rose-400",
    TLE: "text-amber-400",
    "Runtime Error": "text-rose-400",
    "Compilation Error": "text-rose-400",
};

export default function Submissions() {
    const { user } = useAuth();
    const [searchParams] = useSearchParams();
    const problemId = searchParams.get("problemId");
    const [submissions, setSubmissions] = useState([]);
    const [problemsById, setProblemsById] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!user) return;
        setLoading(true);
        const url = problemId ? `/submissions?problemId=${problemId}` : "/submissions";
        Promise.all([api.get(url), api.get("/problems")])
            .then(([subRes, probRes]) => {
                setSubmissions(subRes.data || []);
                const map = {};
                (probRes.data || []).forEach((p) => {
                    map[p.id] = p;
                });
                setProblemsById(map);
            })
            .catch((e) => setError(e.response?.data?.error || e.message))
            .finally(() => setLoading(false));
    }, [user, problemId]);

    if (!user) {
        return (
            <div className="p-8 text-slate-300">
                Please <Link to="/login" className="text-emerald-400 underline">login</Link> to see your submissions.
            </div>
        );
    }

    return (
        <div className="p-8 max-w-5xl mx-auto">
            <h1 className="text-2xl font-bold mb-4">
                My Submissions
                {problemId && problemsById[problemId] && (
                    <span className="text-slate-400 text-lg font-normal ml-2">
                        — {problemsById[problemId].title}
                    </span>
                )}
            </h1>

            {loading && <div className="text-slate-400">Loading...</div>}
            {error && <div className="text-rose-400">{error}</div>}

            {!loading && submissions.length === 0 && (
                <div className="text-slate-400">No submissions yet.</div>
            )}

            {submissions.length > 0 && (
                <div className="overflow-x-auto border border-slate-700 rounded">
                    <table className="w-full text-sm">
                        <thead className="bg-slate-800 text-slate-300">
                            <tr>
                                <th className="text-left px-4 py-2">#</th>
                                <th className="text-left px-4 py-2">Problem</th>
                                <th className="text-left px-4 py-2">Status</th>
                                <th className="text-left px-4 py-2">Time</th>
                                <th className="text-left px-4 py-2">Submitted</th>
                                <th className="text-left px-4 py-2"></th>
                            </tr>
                        </thead>
                        <tbody>
                            {submissions.map((s) => {
                                const prob = problemsById[s.problemId];
                                return (
                                    <tr key={s.id} className="border-t border-slate-700 hover:bg-slate-800/40">
                                        <td className="px-4 py-2 text-slate-400">{s.id}</td>
                                        <td className="px-4 py-2">
                                            {prob ? (
                                                <Link to={`/problems/${prob.id}`} className="text-emerald-400 hover:underline">
                                                    {prob.id}. {prob.title}
                                                </Link>
                                            ) : (
                                                <span className="text-slate-400">#{s.problemId}</span>
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
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
