import axios from 'axios';

// Configurare de bază Axios
axios.defaults.baseURL = 'http://localhost';
//axios.defaults.baseURL = 'http://localhost:8084';
axios.defaults.headers.post['Content-Type'] = 'application/json';

// Interceptor: Adaugă automat token-ul la orice cerere, dacă există
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("auth_token");
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// --- FUNCTIA DE LOGIN (MODIFICATĂ) ---
export const loginUser = async (email, password) => {
    try {
        // 1. Trimitem cererea de login
        const response = await axios.post('/auth/login', { email, password });
        const data = response.data;

        // 2. Verificăm dacă am primit token-ul (Backend-ul returnează: {token, role, userId})
        if (data && data.token) {

            // 3. Salvăm Token-ul
            setAuthToken(data.token);

            // 4. Salvăm detaliile utilizatorului în LocalStorage
            localStorage.setItem("user_role", data.role);
            localStorage.setItem("user_id", data.userId);

            // 🔥 LINIA CRITICĂ LIPSĂ: Salvăm email-ul primit ca parametru al funcției
            localStorage.setItem("user_email", email);

            // (Opțional) Flag de login
            localStorage.setItem("is_logged_in", "true");

            return data;

        } else {
            throw new Error("Răspuns invalid de la server: Lipsă token.");
        }

    } catch (error) {
        // Gestionăm erorile (ex: 401 Unauthorized)
        if (error.response && error.response.status === 401) {
            throw new Error("Credențiale invalide");
        } else {
            // Alte erori (ex: server picat, 500, etc.)
            throw error;
        }
    }
};

// --- HELPER PENTRU SALVARE TOKEN ---
export const setAuthToken = (token) => {
    if (token) {
        // Setăm header-ul default pentru Axios
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        // Salvăm în LocalStorage (să reziste la refresh)
        localStorage.setItem("auth_token", token);
    } else {
        delete axios.defaults.headers.common['Authorization'];
        localStorage.removeItem("auth_token");
    }
};

// --- ALTE FUNCȚII UTILE ---

// Funcție pentru update user (folosită în Admin Dashboard)
export const updateUser = async (id, userData) => {
    try {
        const response = await axios.put(`/people/${id}`, userData);
        return response.data;
    } catch (error) {
        throw error;
    }
};

// Logout: Șterge tot și trimite la pagina de login
export const logout = () => {
    localStorage.clear();
    delete axios.defaults.headers.common['Authorization'];
    window.location.href = "/login";
};

// --- INIȚIALIZARE LA REFRESH ---
// Dacă dăm refresh la pagină, citim token-ul din storage și îl punem înapoi în Axios
const savedToken = localStorage.getItem("auth_token");
if (savedToken) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`;
}

// Getters simpli pentru componentele React
export const getRole = () => localStorage.getItem("user_role");
export const getUserId = () => localStorage.getItem("user_id");
export const isLoggedIn = () => localStorage.getItem("auth_token") !== null;