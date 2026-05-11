import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Login() {
    const [form, setForm] = useState({ username: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const submit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const { data } = await api.post("/auth/login", form);
            login(data);
            navigate("/problems");
        } catch (err) {
            setError(err.response?.data?.error || "Login failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-16 bg-slate-800 p-8 rounded-lg">
            <h2 className="text-2xl font-bold mb-6">Login</h2>
            <form onSubmit={submit} className="space-y-4">
                <input
                    className="w-full p-3 rounded bg-slate-700 text-white"
                    placeholder="Username"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                />
                <input
                    type="password"
                    className="w-full p-3 rounded bg-slate-700 text-white"
                    placeholder="Password"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                />
                {error && <div className="text-red-400 text-sm">{error}</div>}
                <button
                    disabled={loading}
                    className="w-full bg-emerald-600 hover:bg-emerald-700 py-3 rounded font-semibold disabled:opacity-50"
                >
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>
            <p className="mt-4 text-slate-400 text-sm">
                No account? <Link to="/signup" className="text-emerald-400">Sign up</Link>
            </p>
        </div>
    );
}
