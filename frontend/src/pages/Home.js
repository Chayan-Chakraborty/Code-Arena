import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";

const TOPIC_STYLES = {
    Arrays: "bg-emerald-600",
    "Binary Search": "bg-cyan-600",
    "Dynamic Programming": "bg-violet-600",
    Graphs: "bg-fuchsia-600",
    Hashing: "bg-orange-600",
    Heap: "bg-yellow-600 text-slate-950",
    "Linked List": "bg-amber-600",
    Matrix: "bg-rose-600",
    "Prefix Sum": "bg-lime-600 text-slate-950",
    "Sliding Window": "bg-teal-600",
    Stack: "bg-indigo-600",
    Strings: "bg-sky-600",
    Trees: "bg-pink-600",
};

export default function Home() {
    const [topics, setTopics] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.get("/problems")
            .then((r) => {
                const counts = r.data.reduce((acc, problem) => {
                    acc[problem.topic] = (acc[problem.topic] || 0) + 1;
                    return acc;
                }, {});

                const topicCards = Object.entries(counts)
                    .sort((a, b) => a[0].localeCompare(b[0]))
                    .map(([name, count]) => ({
                        name,
                        count,
                        color: TOPIC_STYLES[name] || "bg-slate-700",
                    }));

                setTopics(topicCards);
            })
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="p-10">
            <h1 className="text-4xl font-bold mb-2">Welcome to Code Arena</h1>
            <p className="text-slate-400 mb-8">
                Sharpen your coding skills with real problems, a real compiler, and instant feedback.
            </p>

            <div className="flex items-end justify-between gap-4 mb-4">
                <div>
                    <h2 className="text-2xl font-semibold">Topics</h2>
                    <p className="text-slate-400 text-sm mt-1">
                        Explore the full problem bank by data structure and pattern.
                    </p>
                </div>
                {!loading && (
                    <Link to="/problems" className="text-emerald-400 hover:underline text-sm">
                        View all problems
                    </Link>
                )}
            </div>

            {loading ? (
                <div className="text-slate-400">Loading topics...</div>
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {topics.map((topic) => (
                        <Link
                            key={topic.name}
                            to={`/problems?topic=${encodeURIComponent(topic.name)}`}
                            className={`${topic.color} hover:opacity-90 rounded-lg p-6 shadow`}
                        >
                            <h3 className="text-xl font-bold">{topic.name}</h3>
                            <p className="text-sm opacity-90 mt-2">{topic.count} problems</p>
                            <p className="text-sm opacity-80 mt-1">View problems →</p>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
