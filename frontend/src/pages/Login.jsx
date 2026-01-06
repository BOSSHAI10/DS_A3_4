import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
// --- MODIFICARE AICI: Importăm loginUser, nu handleLoginResponse ---
import { loginUser } from '../axios_helper';

export default function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        try {
            // --- MODIFICARE AICI: Apelăm loginUser ---
            const user = await loginUser(email, password);

            // Redirecționare în funcție de rol
            if (user.role === "ADMIN") {
                navigate("/admin");
            } else {
                navigate("/client");
            }
        } catch (err) {
            console.error(err);
            setError("Login eșuat! Verifică emailul/parola sau conexiunea.");
        }
    };

    return (
        <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
            <div className="card p-4 shadow" style={{width: '350px'}}>
                <h3 className="text-center mb-4 text-primary">Energy System</h3>
                {error && <div className="alert alert-danger">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label">Email</label>
                        <input
                            type="email"
                            className="form-control"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Parolă</label>
                        <input
                            type="password"
                            className="form-control"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-primary w-100">Autentificare</button>
                </form>
            </div>
        </div>
    );
}