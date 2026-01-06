import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Admin from './pages/Admin';
import Client from './pages/Client';
import { isLoggedIn, getRole } from './axios_helper'; // Funcțiile create anterior

// Componentă specială pentru a proteja rutele
// Verifică (1) dacă ești logat și (2) dacă ai rolul necesar
const ProtectedRoute = ({ children, roleRequired }) => {

    // 1. Dacă nu e logat deloc -> Trimite-l la Login
    if (!isLoggedIn()) {
        return <Navigate to="/login" />;
    }

    // 2. Dacă e logat, dar nu are rolul cerut
    const currentRole = getRole();
    if (roleRequired && currentRole !== roleRequired) {
        // Ești ADMIN dar încerci să intri la Client? -> Mergi la Admin
        if (currentRole === 'ADMIN') return <Navigate to="/admin" />;

        // Ești USER dar încerci să intri la Admin? -> Mergi la Client
        if (currentRole === 'USER') return <Navigate to="/client" />;

        return <Navigate to="/login" />;
    }

    // 3. Totul e OK -> Afișează pagina
    return children;
};

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Ruta Default (/): Redirecționează inteligent în funcție de stare */}
                <Route path="/" element={
                    isLoggedIn() ? (
                        getRole() === 'ADMIN' ? <Navigate to="/admin" /> : <Navigate to="/client" />
                    ) : (
                        <Navigate to="/login" />
                    )
                } />

                {/* Ruta Publică */}
                <Route path="/login" element={<Login />} />

                {/* Rute pentru ADMINISTRATOR */}
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute roleRequired="ADMIN">
                            <Admin />
                        </ProtectedRoute>
                    }
                />

                {/* Rute pentru CLIENT (User) */}
                <Route
                    path="/client"
                    element={
                        <ProtectedRoute roleRequired="USER">
                            <Client />
                        </ProtectedRoute>
                    }
                />

                {/* Orice alt link invalid -> Trimite la Login */}
                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;