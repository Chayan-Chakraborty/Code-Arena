import React, { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const username = localStorage.getItem("username");
        const userId = localStorage.getItem("userId");
        const role = localStorage.getItem("role") || "USER";
        if (token && username) setUser({ token, username, userId, role });
    }, []);

    const login = ({ token, username, userId, role }) => {
        const resolvedRole = role || "USER";
        localStorage.setItem("token", token);
        localStorage.setItem("username", username);
        localStorage.setItem("userId", userId);
        localStorage.setItem("role", resolvedRole);
        setUser({ token, username, userId, role: resolvedRole });
    };

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("userId");
        localStorage.removeItem("role");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);
