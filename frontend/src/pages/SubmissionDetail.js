import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const STATUS_COLOR = {
    Accepted: "text-emerald-400",
    "Wrong Answer": "text-rose-400",
    TLE: "text-amber-400",
    "Runtime Error": "text-rose-400",
    "Compilation Error": "text-rose-400",
};

export default function SubmissionDetail() {
    const { id } = useParams();
    const { user } = useAuth();
    const [submission, setSubmission] = useState(null);
    const [problem, setProblem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        if (!user) return;
        setLoading(true);
        api.get(`/submissions/${id}`)
            .then((r) => {
                setSubmission(r.data);
                if (r.data?.problemId) {
                    return api.get(`/problems/${r.data.problemId}`).then((p) => setProblem(p.data));
                }
            })
            .catch((e) => setError(e.response?.data?.error || e.message))
            .finally(() => setLoading(false));
    }, [id, user]);

    if (!user) {
        return (
            <div className="p-8 text-slate-300">
                Please <Link to="/login" className="text-emerald-400 underline">login</Link> to view submissions.
            </div>
        );
    }
    if (loading) return <div className="p-8 text-slate-400">Loading...</div>;
    if (error) return <div className="p-8 text-rose-400">{error}</div>;
    if (!submission) return <div className="p-8 text-rose-400">Submission not found</div>;

    const onCopy = async () => {
        try {
            await navigator.clipboard.writeText(submission.code || "");
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch (_) {
            // ignore
        }
    };

    return (
        <div className="p-6 max-w-5xl mx-auto">
            <div className="mb-4">
                <Link to="/submissions" className="text-emerald-400 hover:underline text-sm">
                    ← Back to submissions
                </Link>
            </div>

            <div className="flex flex-wrap items-center gap-4 mb-4">
                <h1 className="text-2xl font-bold">Submission #{submission.id}</h1>
                <span className={`font-semibold ${STATUS_COLOR[submission.status] || "text-slate-300"}`}>
                    {submission.status}
                </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4 text-sm">
                <div className="bg-slate-800 rounded p-3">
                    <div className="text-slate-400">Problem</div>
                    <div className="font-semibold">
                        {problem ? (
                            <Link to={`/problems/${problem.id}`} className="text-emerald-400 hover:underline">
                                {problem.id}. {problem.title}
                            </Link>
                        ) : (
                            `#${submission.problemId}`
                        )}
                    </div>
                </div>
                <div className="bg-slate-800 rounded p-3">
                    <div className="text-slate-400">Execution time</div>
                    <div className="font-semibold">
                        {submission.executionTime != null ? `${Number(submission.executionTime).toFixed(3)}s` : "—"}
                    </div>
                </div>
                <div className="bg-slate-800 rounded p-3">
                    <div className="text-slate-400">Submitted</div>
                    <div className="font-semibold">
                        {submission.createdAt ? new Date(submission.createdAt).toLocaleString() : "—"}
                    </div>
                </div>
            </div>

            <div className="flex items-center justify-between mb-2">
                <h2 className="text-lg font-semibold">Code</h2>
                <button
                    onClick={onCopy}
                    className="bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded text-sm"
                >
                    {copied ? "Copied!" : "Copy"}
                </button>
            </div>
            <div className="border border-slate-700 rounded overflow-hidden">
                <Editor
                    height="500px"
                    language="java"
                    theme="vs-dark"
                    value={submission.code || ""}
                    options={{
                        readOnly: true,
                        fontSize: 14,
                        minimap: { enabled: false },
                        automaticLayout: true,
                    }}
                />
            </div>
        </div>
    );
}
