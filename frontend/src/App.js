import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Problems from "./pages/Problems";
import ProblemDetail from "./pages/ProblemDetail";
import Dashboard from "./pages/Dashboard";
import Submissions from "./pages/Submissions";
import SubmissionDetail from "./pages/SubmissionDetail";

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Navbar />
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/signup" element={<Signup />} />
                    <Route path="/problems" element={<Problems />} />
                    <Route path="/problems/:id" element={<ProblemDetail />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/submissions" element={<Submissions />} />
                    <Route path="/submissions/:id" element={<SubmissionDetail />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
