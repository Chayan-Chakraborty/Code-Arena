import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const STATUS_COLOR = {
    PENDING: "text-amber-400",
    APPROVED: "text-emerald-400",
    REJECTED: "text-rose-400",
};

export default function AdminProblems() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [problems, setProblems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [busyId, setBusyId] = useState(null);
    const [filter, setFilter] = useState("pending"); // pending | all

    useEffect(() => {
        if (!user) {
            navigate("/login");
            return;
        }
        if (user.role !== "ADMIN") {
            navigate("/problems");
        }
    }, [user, navigate]);

    const load = async () => {
        setLoading(true);
        setError("");
        try {
            const { data } = await api.get(`/problems?status=${filter}`);
            setProblems(data || []);
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user?.role === "ADMIN") load();
        // eslint-disable-next-line
    }, [user, filter]);

    const act = async (id, action) => {
        setBusyId(id);
        setError("");
        try {
            await api.post(`/problems/${id}/${action}`);
            await load();
        } catch (e) {
            setError(e.response?.data?.error || e.message);
        } finally {
            setBusyId(null);
        }
    };

    if (user?.role !== "ADMIN") return null;

    return (
        <div className="p-8 max-w-6xl mx-auto">
            <h1 className="text-3xl font-bold mb-2">Admin · Moderation</h1>
            <p className="text-slate-400 mb-4">Review user-submitted problems.</p>

            <div className="flex gap-2 mb-4">
                <button
                    onClick={() => setFilter("pending")}
                    className={`px-3 py-1 rounded text-sm ${filter === "pending" ? "bg-emerald-600" : "bg-slate-800 border border-slate-700"}`}
                >Pending</button>
                <button
                    onClick={() => setFilter("all")}
                    className={`px-3 py-1 rounded text-sm ${filter === "all" ? "bg-emerald-600" : "bg-slate-800 border border-slate-700"}`}
                >All</button>
                <button onClick={load} className="ml-auto bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-sm">
                    Refresh
                </button>
            </div>

            {error && <div className="text-red-400 mb-3">{error}</div>}
            {loading && <div className="text-slate-400">Loading...</div>}

            <div className="bg-slate-800 rounded-lg overflow-hidden">
                <table className="w-full text-left">
                    <thead className="bg-slate-700">
                        <tr>
                            <th className="p-3">#</th>
                            <th className="p-3">Title</th>
                            <th className="p-3">Topic</th>
                            <th className="p-3">Difficulty</th>
                            <th className="p-3">Status</th>
                            <th className="p-3">By</th>
                            <th className="p-3 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {problems.map((p) => (
                            <tr key={p.id} className="border-t border-slate-700">
                                <td className="p-3 text-slate-400">{p.id}</td>
                                <td className="p-3">
                                    <Link to={`/problems/${p.id}`} className="text-emerald-400 hover:underline">
                                        {p.title}
                                    </Link>
                                </td>
                                <td className="p-3 text-slate-300">{p.topic}</td>
                                <td className="p-3 text-slate-300">{p.difficulty}</td>
                                <td className={`p-3 font-semibold ${STATUS_COLOR[p.status] || ""}`}>{p.status}</td>
                                <td className="p-3 text-slate-400">{p.createdBy ?? "—"}</td>
                                <td className="p-3 text-right">
                                    {p.status === "PENDING" && (
                                        <div className="inline-flex gap-2">
                                            <button
                                                disabled={busyId === p.id}
                                                onClick={() => act(p.id, "approve")}
                                                className="bg-emerald-600 hover:bg-emerald-700 px-3 py-1 rounded text-xs font-semibold disabled:opacity-50"
                                            >Approve</button>
                                            <button
                                                disabled={busyId === p.id}
                                                onClick={() => act(p.id, "reject")}
                                                className="bg-rose-700 hover:bg-rose-800 px-3 py-1 rounded text-xs disabled:opacity-50"
                                            >Reject</button>
                                        </div>
                                    )}
                                    {p.status === "REJECTED" && (
                                        <button
                                            disabled={busyId === p.id}
                                            onClick={() => act(p.id, "approve")}
                                            className="bg-emerald-600 hover:bg-emerald-700 px-3 py-1 rounded text-xs disabled:opacity-50"
                                        >Approve</button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        {!loading && problems.length === 0 && (
                            <tr><td colSpan="7" className="p-6 text-center text-slate-400">No problems.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
