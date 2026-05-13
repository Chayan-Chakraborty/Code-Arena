import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    return (
        <nav className="bg-slate-800 px-6 py-3 flex justify-between items-center border-b border-slate-700">
            <Link to="/" className="text-xl font-bold text-emerald-400">
                Code Arena
            </Link>
            <div className="flex gap-4 items-center">
                <Link to="/problems" className="hover:text-emerald-400">Problems</Link>
                {user && <Link to="/submissions" className="hover:text-emerald-400">Submissions</Link>}
                {user && <Link to="/dashboard" className="hover:text-emerald-400">Dashboard</Link>}
                {user ? (
                    <>
                        <span className="text-slate-300">@{user.username}</span>
                        <button
                            onClick={() => { logout(); navigate("/"); }}
                            className="bg-red-600 hover:bg-red-700 px-3 py-1 rounded"
                        >
                            Logout
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" className="hover:text-emerald-400">Login</Link>
                        <Link to="/signup" className="bg-emerald-600 hover:bg-emerald-700 px-3 py-1 rounded">
                            Sign Up
                        </Link>
                    </>
                )}
            </div>
        </nav>
    );
}
