import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Modal, Button, Form } from 'react-bootstrap';
import { logout } from '../axios_helper';

export default function Client() {
    const navigate = useNavigate();

    // --- STĂRI (STATE) ---
    const [myDevices, setMyDevices] = useState([]);
    const [userName, setUserName] = useState('');

    // Stări pentru schimbare parolă
    const [showPassModal, setShowPassModal] = useState(false);
    const [passData, setPassData] = useState({ oldPassword: '', newPassword: '' });

    const userEmail = localStorage.getItem("user_email");

    // --- INITIALIZARE ---
    useEffect(() => {
        if (userEmail) {
            // 1. Cerem dispozitivele
            axios.get(`/devices/user/${userEmail}`)
                .then(res => setMyDevices(res.data))
                .catch(e => console.error("Eroare la preluare dispozitive:", e));

            // 2. Cerem numele (pentru afișare corectă)
            axios.get(`/people/by-email/${userEmail}`)
                .then(res => {
                    if (res.data && res.data.name) {
                        setUserName(res.data.name);
                    }
                })
                .catch(e => console.log("Nu s-a putut prelua numele."));
        }
    }, [userEmail]);

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    // --- LOGICA DE SCHIMBARE PAROLĂ ---
    const handleChangePassword = async () => {
        if (!passData.oldPassword || !passData.newPassword) {
            alert("Te rog completează ambele câmpuri!");
            return;
        }

        try {
            await axios.post("/auth/change-password", {
                email: userEmail,
                oldPassword: passData.oldPassword,
                newPassword: passData.newPassword
            });

            alert("Parola a fost schimbată cu succes! Te rugăm să te autentifici din nou.");
            setShowPassModal(false);
            handleLogout();

        } catch (e) {
            console.error(e);
            const errorMsg = e.response && e.response.data ? e.response.data : "Eroare la schimbarea parolei.";
            alert("Eroare: " + errorMsg);
        }
    };

    return (
        <div className="container mt-4">
            {/* HEADER */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>Dispozitivele Mele</h2>
                <div>
                    {/* --- AICI ESTE MODIFICAREA CERUTĂ --- */}
                    <span className="me-3 fw-bold text-secondary">
                        Ești logat ca : {userName || userEmail}
                    </span>

                    <button
                        className="btn btn-warning px-3 me-2 fw-bold text-white"
                        onClick={() => setShowPassModal(true)}
                    >
                        🔑 Schimbă Parola
                    </button>

                    <button className="btn btn-danger px-4" onClick={handleLogout}>Logout</button>
                </div>
            </div>

            <hr/>

            {/* LISTA DE DISPOZITIVE (Grafica neschimbată) */}
            <div className="row mt-3">
                {myDevices.length === 0 && (
                    <div className="col-12 text-center text-muted mt-5">
                        <h4>Nu ai niciun dispozitiv atribuit.</h4>
                        <p>Contactează administratorul pentru alocare.</p>
                    </div>
                )}

                {myDevices.map(d => (
                    <div key={d.id} className="col-md-4 mb-4">
                        <div
                            className="card h-100"
                            style={{
                                border: "2px solid black",
                                boxShadow: "5px 5px 5px black"
                            }}
                        >
                            <div className="card-body text-center d-flex flex-column justify-content-center">
                                <h5 className="card-title fw-bold text-dark">{d.name}</h5>
                                <h2 className="text-primary my-3">{d.consumption} kW</h2>
                                <div>
                                    <span className={`badge px-3 py-2 ${d.active ? 'bg-success' : 'bg-danger'}`}>
                                        {d.active ? 'ACTIV' : 'INACTIV'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* --- MODAL SCHIMBARE PAROLĂ --- */}
            <Modal show={showPassModal} onHide={() => setShowPassModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Schimbare Parolă</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Parola Veche</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Introdu parola actuală"
                                value={passData.oldPassword}
                                onChange={e => setPassData({...passData, oldPassword: e.target.value})}
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Parola Nouă</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Introdu noua parolă"
                                value={passData.newPassword}
                                onChange={e => setPassData({...passData, newPassword: e.target.value})}
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowPassModal(false)}>Renunță</Button>
                    <Button variant="success" onClick={handleChangePassword}>Schimbă</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}