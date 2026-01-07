import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Modal, Button, Form } from 'react-bootstrap';
import SockJS from 'sockjs-client'; // <--- IMPORT NOU
import Stomp from 'stompjs';        // <--- IMPORT NOU
import { logout, getAuthToken } from '../axios_helper';

// URL-ul WebSocket (prin Gateway pe portul 80 sau direct 8086)
const SOCKET_URL = 'http://localhost:80/chat/ws';

export default function Client() {
    const navigate = useNavigate();

    // --- STĂRI EXISTENTE ---
    const [myDevices, setMyDevices] = useState([]);
    const [userName, setUserName] = useState('');
    const [showPassModal, setShowPassModal] = useState(false);
    const [passData, setPassData] = useState({ oldPassword: '', newPassword: '' });

    // --- STĂRI NOI PENTRU CHAT ---
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [connected, setConnected] = useState(false);

    // Referințe pentru WebSocket
    const stompClientRef = useRef(null);
    const messagesEndRef = useRef(null);

    const userEmail = localStorage.getItem("user_email");

    // --- INITIALIZARE DATE USER ---
    useEffect(() => {
        if (userEmail) {
            axios.get(`/devices/user/${userEmail}`)
                .then(res => setMyDevices(res.data))
                .catch(e => console.error("Eroare la preluare dispozitive:", e));

            axios.get(`/people/by-email/${userEmail}`)
                .then(res => {
                    if (res.data && res.data.name) {
                        setUserName(res.data.name);
                    }
                })
                .catch(e => console.log("Nu s-a putut prelua numele."));
        }
    }, [userEmail]);

    // --- LOGICA CHAT (Connect / Disconnect) ---
    useEffect(() => {
        // Auto-scroll la ultimul mesaj
        if (isChatOpen && messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
    }, [chatMessages, isChatOpen]);

    const connectToChat = () => {
        if (connected) return;

        const socket = new SockJS(SOCKET_URL);
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Oprim logurile din consolă

        const token = getAuthToken();

        stompClient.connect({ 'Authorization': `Bearer ${token}` }, () => {
            setConnected(true);
            stompClientRef.current = stompClient;

            // 1. Abonare la mesajele private (răspunsuri de la Admin)
            stompClient.subscribe('/user/queue/private', (payload) => {
                const message = JSON.parse(payload.body);
                setChatMessages(prev => [...prev, message]);
            });

            // 2. Încărcare istoric conversație cu Admin
            // URL-ul backend este /chat/history/{currentUser}/{remoteUser}
            // Aici presupunem că backend-ul știe cine e "Eu" din token, dar endpoint-ul cere parametrii.
            // receiverId pentru admin este string-ul "admin"
            axios.get(`/chat/history/${userEmail}/admin`, {
                headers: { 'Authorization': `Bearer ${token}` }
            }).then(res => {
                setChatMessages(res.data);
            }).catch(err => console.log("Nu am putut încărca istoricul", err));

        }, (err) => {
            console.error("Eroare conexiune chat:", err);
            setConnected(false);
        });
    };

    const toggleChat = () => {
        if (!isChatOpen) {
            connectToChat();
        }
        setIsChatOpen(!isChatOpen);
    };

    const sendMessage = () => {
        if (chatInput.trim() && stompClientRef.current) {
            const messagePayload = {
                receiverId: "admin", // Trimitem mereu către Admin
                content: chatInput
            };

            stompClientRef.current.send("/app/private-message", {}, JSON.stringify(messagePayload));

            // Adăugăm mesajul local pentru feedback instant
            setChatMessages(prev => [...prev, {
                senderId: userEmail,
                content: chatInput,
                timestamp: new Date().toISOString()
            }]);
            setChatInput("");
        }
    };

    // --- LOGICA LOGOUT / SCHIMBARE PAROLĂ ---
    const handleLogout = () => {
        if (stompClientRef.current) {
            stompClientRef.current.disconnect();
        }
        logout();
        navigate("/login");
    };

    const handleChangePassword = () => {
        // ... (Logica existentă de schimbare parolă păstrată intactă)
        // Presupunând endpoint-ul /auth/change-password sau similar
        // Pentru demo, doar închidem modalul
        setShowPassModal(false);
        alert("Funcționalitatea de schimbare parolă trebuie implementată în backend (Auth Service).");
    };

    return (
        <div className="container mt-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1>Salut, {userName || "Client"}!</h1>
                <div>
                    <Button variant="warning" className="me-2" onClick={() => setShowPassModal(true)}>
                        Schimbă Parola
                    </Button>
                    <Button variant="danger" onClick={handleLogout}>
                        Logout
                    </Button>
                </div>
            </div>

            <h3>Dispozitivele mele</h3>
            {myDevices.length === 0 ? (
                <p>Nu ai niciun dispozitiv asociat.</p>
            ) : (
                <div className="row">
                    {myDevices.map(device => (
                        <div key={device.id} className="col-md-4 mb-3">
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">{device.name}</h5>
                                    <p className="card-text">
                                        Consum: {device.consumption} kW <br />
                                        Status: {device.active ?
                                        <span className="text-success fw-bold">Activ</span> :
                                        <span className="text-danger fw-bold">Inactiv</span>}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* --- MODAL SCHIMBARE PAROLĂ (EXISTENT) --- */}
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
                                value={passData.oldPassword}
                                onChange={e => setPassData({...passData, oldPassword: e.target.value})}
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Parola Nouă</Form.Label>
                            <Form.Control
                                type="password"
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

            {/* --- BUTON CHAT FLOTANT (NOU) --- */}
            <div style={{ position: 'fixed', bottom: '30px', right: '30px', zIndex: 1000 }}>
                <Button
                    variant="primary"
                    onClick={toggleChat}
                    style={{ borderRadius: '50%', width: '60px', height: '60px', fontSize: '24px', boxShadow: '0 4px 8px rgba(0,0,0,0.3)' }}
                >
                    💬
                </Button>
            </div>

            {/* --- FEREASTRA DE CHAT (NOU) --- */}
            {isChatOpen && (
                <div style={{
                    position: 'fixed', bottom: '100px', right: '30px', width: '350px', height: '450px',
                    backgroundColor: 'white', border: '1px solid #ccc', borderRadius: '10px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.2)', display: 'flex', flexDirection: 'column', zIndex: 1000
                }}>
                    <div style={{ padding: '10px', background: '#0d6efd', color: 'white', borderTopLeftRadius: '10px', borderTopRightRadius: '10px', display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ fontWeight: 'bold' }}>Suport Clienți</span>
                        <button onClick={toggleChat} style={{ background: 'transparent', border: 'none', color: 'white', cursor: 'pointer', fontWeight: 'bold' }}>✕</button>
                    </div>

                    <div style={{ flex: 1, padding: '15px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px', backgroundColor: '#f8f9fa' }}>
                        {chatMessages.length === 0 && <div className="text-center text-muted mt-3">Începe o conversație cu un operator.</div>}

                        {chatMessages.map((msg, idx) => {
                            // Identificăm dacă mesajul e de la mine sau de la admin
                            const isMe = msg.senderId === userEmail || msg.senderId === userName;
                            return (
                                <div key={idx} style={{
                                    alignSelf: isMe ? 'flex-end' : 'flex-start',
                                    backgroundColor: isMe ? '#0d6efd' : '#e9ecef',
                                    color: isMe ? 'white' : 'black',
                                    padding: '8px 12px',
                                    borderRadius: '15px',
                                    maxWidth: '80%',
                                    fontSize: '0.9rem',
                                    borderBottomRightRadius: isMe ? '0' : '15px',
                                    borderBottomLeftRadius: isMe ? '15px' : '0'
                                }}>
                                    {msg.content}
                                </div>
                            );
                        })}
                        <div ref={messagesEndRef} />
                    </div>

                    <div style={{ padding: '10px', borderTop: '1px solid #eee', display: 'flex', background: 'white', borderBottomLeftRadius: '10px', borderBottomRightRadius: '10px' }}>
                        <input
                            type="text"
                            className="form-control"
                            value={chatInput}
                            onChange={(e) => setChatInput(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
                            placeholder="Scrie un mesaj..."
                            style={{ borderRadius: '20px' }}
                        />
                        <button onClick={sendMessage} style={{ marginLeft: '10px', background: 'none', border: 'none', color: '#0d6efd', fontSize: '1.2rem', cursor: 'pointer' }}>➤</button>
                    </div>
                </div>
            )}
        </div>
    );
}