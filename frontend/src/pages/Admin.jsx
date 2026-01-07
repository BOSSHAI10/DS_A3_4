import { useEffect, useState, useRef } from 'react'; // <--- IMPORT useRef
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Modal, Button, Form } from 'react-bootstrap';
import { logout } from '../axios_helper';

export default function Admin() {
    const navigate = useNavigate();
    const fileInputRef = useRef(null); // <--- Referință pentru input-ul de fișier

    // --- STĂRI (STATE) ---
    //const [users, setUsers] = useState([]);
    //const [devices, setDevices] = useState([]);
    //const [selectedUserForDevice, setSelectedUserForDevice] = useState({});

    // Stări Modale
    //const [showViewModal, setShowViewModal] = useState(false);
    //const [showEditModal, setShowEditModal] = useState(false);
    //const [currentUser, setCurrentUser] = useState({ id: '', name: '', email: '', age: 0, role: 'USER' });

    //const [showCreateDeviceModal, setShowCreateDeviceModal] = useState(false);
    //const [showViewDeviceModal, setShowViewDeviceModal] = useState(false);
    //const [showEditDeviceModal, setShowEditDeviceModal] = useState(false);
    //const [currentDevice, setCurrentDevice] = useState({ id: '', name: '', consumption: 0, active: true });
    //const [newDevice, setNewDevice] = useState({ name: '', consumption: '' });

    // --- STĂRI EXISTENTE (USERS & DEVICES) ---
    const [users, setUsers] = useState([]);
    const [devices, setDevices] = useState([]);
    const [selectedUserForDevice, setSelectedUserForDevice] = useState({});

    // Stări Modale Useri
    const [showViewModal, setShowViewModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [currentUser, setCurrentUser] = useState({ id: '', name: '', email: '', age: 0, role: 'USER' });

    // Stări Modale Dispozitive
    const [showCreateDeviceModal, setShowCreateDeviceModal] = useState(false);
    const [showViewDeviceModal, setShowViewDeviceModal] = useState(false);
    const [showEditDeviceModal, setShowEditDeviceModal] = useState(false);
    const [currentDevice, setCurrentDevice] = useState({ id: '', name: '', consumption: 0, active: true });
    const [newDevice, setNewDevice] = useState({ name: '', consumption: '' });

    // --- STĂRI NOI PENTRU CHAT ADMIN ---
    const [chatUsers, setChatUsers] = useState([]); // Lista userilor care au scris
    const [selectedChatUser, setSelectedChatUser] = useState(null);
    const [adminMessages, setAdminMessages] = useState([]);
    const [adminInput, setAdminInput] = useState("");
    const stompClientRef = useRef(null);
    const messagesEndRef = useRef(null);

    // --- INITIALIZARE ---
    // 1. Definește funcția fetchData în afara useEffect ca să o poți apela oricând
    // --- INITIALIZARE ---
    const fetchData = async () => {
        const token = window.localStorage.getItem("auth_token");
        if (!token) {
            navigate("/login");
            return;
        }

        try {
            const usersRes = await axios.get('/people');
            setUsers(usersRes.data);
            const devicesRes = await axios.get('/devices');
            setDevices(devicesRes.data);

            // Maparea userilor pe dispozitive (dacă e nevoie pentru afișare)
            const map = {};
            devicesRes.data.forEach(d => {
                if(d.userId) map[d.id] = d.userId;
            });
            setSelectedUserForDevice(map);

            // Fetch Conversații Active pentru Chat
            const conversationsRes = await axios.get('/chat/admin/conversations');
            setChatUsers(conversationsRes.data);

        } catch (error) {
            console.error("Eroare la fetch data:", error);
            if(error.response && error.response.status === 403) {
                alert("Nu ai drepturi de admin!");
                logout();
                navigate("/login");
            }
        }
    };

    useEffect(() => {
        fetchData();
        connectToChat(); // Conectăm Adminul la Chat la pornire
        return () => {
            if (stompClientRef.current) stompClientRef.current.disconnect();
        };
    }, []);

    // --- LOGICA WEBSOCKET ADMIN ---
    const connectToChat = () => {
        const socket = new SockJS(SOCKET_URL);
        const stompClient = Stomp.over(socket);
        stompClient.debug = null;
        const token = getAuthToken();

        stompClient.connect({ 'Authorization': `Bearer ${token}` }, () => {
            stompClientRef.current = stompClient;

            // Ascultă TOATE mesajele care vin către Admin
            stompClient.subscribe('/user/queue/private', (payload) => {
                const message = JSON.parse(payload.body);

                // 1. Dacă mesajul e de la userul selectat, îl punem pe ecran
                if (selectedChatUser && message.senderId === selectedChatUser) {
                    setAdminMessages(prev => [...prev, message]);
                }

                // 2. Dacă e un user nou, îl adăugăm în listă
                setChatUsers(prev => {
                    // Evităm duplicatele și nu ne adăugăm pe noi (admin)
                    if (!prev.includes(message.senderId) && message.senderId !== 'admin') {
                        return [...prev, message.senderId];
                    }
                    return prev;
                });
            });
        }, err => console.error("Eroare Chat:", err));
    };

    // Auto-scroll chat
    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
    }, [adminMessages]);

    // Selectare User din listă
    const selectUserToChat = (userId) => {
        setSelectedChatUser(userId);
        // Încărcăm istoricul: /chat/history/admin/userId
        axios.get(`/chat/history/admin/${userId}`).then(res => {
            setAdminMessages(res.data);
        }).catch(err => console.error("Err istoric:", err));
    };

    // Trimitere mesaj Admin -> User
    const sendAdminMessage = () => {
        if (adminInput.trim() && selectedChatUser && stompClientRef.current) {
            const messagePayload = {
                receiverId: selectedChatUser,
                content: adminInput
            };

            stompClientRef.current.send("/app/private-message", {}, JSON.stringify(messagePayload));

            setAdminMessages(prev => [...prev, {
                senderId: "admin", // sau ID-ul adminului real
                content: adminInput,
                timestamp: new Date().toISOString()
            }]);
            setAdminInput("");
        }
    };

    // 2. Apelează funcția la încărcarea paginii
    useEffect(() => {
        fetchData();
    }, [navigate]);
    /*
    const fetchData = async () => {
        try {
            const uRes = await axios.get("/people");
            const dRes = await axios.get("/devices");
            setUsers(uRes.data);
            setDevices(dRes.data);
        } catch (e) {
            if (e.response && e.response.status === 401) handleLogout();
        }
    };
    */


    const handleLogout = () => { logout(); navigate("/login"); };

    // --- FUNCȚIE BACKUP (EXISTENTĂ) ---
    const handleExportData = async () => {
        try {
            const usersReq = axios.get("/people");
            const devicesReq = axios.get("/devices");
            const authReq = axios.get("/auth/all");

            const [usersRes, devicesRes, authRes] = await Promise.all([usersReq, devicesReq, authReq]);

            const backupData = {
                timestamp: new Date().toISOString(),
                credentials: authRes.data,
                user_profiles: usersRes.data,
                devices: devicesRes.data
            };

            const jsonString = JSON.stringify(backupData, null, 2);
            const blob = new Blob([jsonString], { type: "application/json" });
            const url = URL.createObjectURL(blob);

            const link = document.createElement('a');
            link.href = url;
            const timestamp = new Date().toISOString().replace(/T/, '_').replace(/:/g, '-').split('.')[0];
            link.download = `Sistem_Energetic_Backup_${timestamp}.json`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (e) { alert("Eroare backup: " + e.message); }
    };

    // --- FUNCȚIE RESTORE (NOUĂ) ---
    const handleFileChange = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = async (e) => {
            try {
                const data = JSON.parse(e.target.result);

                if (!confirm(`Sigur vrei să încarci datele din ${data.timestamp}? \n(Asigură-te că baza e goală pentru a evita duplicatele)`)) return;

                // 1. Restaurăm Credențialele (Auth)
                if (data.credentials && data.credentials.length > 0) {
                    await axios.post("/auth/restore", data.credentials);
                    console.log("Credențiale restaurate.");
                }

                // 2. Restaurăm Profilurile Utilizatorilor
                if (data.user_profiles && data.user_profiles.length > 0) {
                    for (const user of data.user_profiles) {
                        // Folosim endpoint-ul de creare existent
                        // Backend-ul va genera un ID nou, dar email-ul rămâne cheia de legătură
                        try {
                            await axios.post("/people", {
                                name: user.name,
                                email: user.email,
                                age: user.age,
                                role: user.role
                            });
                        } catch (err) { console.warn("User existent sau eroare:", user.email); }
                    }
                    console.log("Utilizatori restaurați.");
                }

                // 3. Restaurăm Dispozitivele
                if (data.devices && data.devices.length > 0) {
                    for (const dev of data.devices) {
                        try {
                            // Creăm dispozitivul
                            await axios.post("/devices", {
                                name: dev.name,
                                consumption: dev.consumption,
                                active: dev.active,
                                username: dev.username // Legătura prin email se păstrează!
                            });
                        } catch (err) { console.warn("Device eroare:", dev.name); }
                    }
                    console.log("Dispozitive restaurate.");
                }

                await fetchData();

                // 3. Resetăm input-ul de fișier ca să poți face upload din nou dacă vrei
                if (fileInputRef.current) {
                    fileInputRef.current.value = "";
                }

                alert("Restaurare completă! Pagina se va reîncărca.");
                window.location.reload();

            } catch (err) {
                console.error(err);
                alert("Fișier invalid sau eroare la încărcare.");
            }
        };
        reader.readAsText(file);
        // Resetăm input-ul ca să putem selecta același fișier din nou dacă e nevoie
        event.target.value = null;
    };

    // Handlers Useri (neschimbați)
    const handleCreateUser = async () => {
        const name = prompt("Nume utilizator:");
        const email = prompt("Email:");
        const age = prompt("Varsta:");
        if(name && email) {
            try { await axios.post("/people", { name, email, age: parseInt(age), role: "USER" }); fetchData(); } catch (e) {}
        }
    };
    const handleDeleteUser = async (id) => { if (confirm("Stergi?")) { await axios.delete(`/people/${id}`); fetchData(); } };
    const handleViewClick = (user) => { setCurrentUser(user); setShowViewModal(true); };
    const handleEditClick = (user) => { setCurrentUser(user); setShowEditModal(true); };
    const handleSaveUserChanges = async () => { await axios.put(`/people/${currentUser.id}`, currentUser); setShowEditModal(false); fetchData(); };
    const handleUserChange = (e) => { setCurrentUser(prev => ({ ...prev, [e.target.name]: e.target.value })); };

    // Handlers Dispozitive (neschimbați)
    const handleSaveNewDevice = async () => {
        if (!newDevice.name) return;
        await axios.post('/devices', { name: newDevice.name, consumption: parseInt(newDevice.consumption), active: true, username: null });
        setShowCreateDeviceModal(false); fetchData(); setNewDevice({ name: '', consumption: '' });
    };
    const handleDeleteDevice = async (id) => { if (confirm("Stergi?")) { await axios.delete(`/devices/${id}`); fetchData(); } };
    const handleDeviceViewClick = (d) => { setCurrentDevice(d); setShowViewDeviceModal(true); };
    const handleDeviceEditClick = (d) => { setCurrentDevice(d); setShowEditDeviceModal(true); };
    const handleSaveDeviceChanges = async () => {
        await axios.put(`/devices/${currentDevice.id}`, { name: currentDevice.name, consumption: parseInt(currentDevice.consumption), active: currentDevice.active });
        setShowEditDeviceModal(false); fetchData();
    };
    const handleDeviceEditChange = (e) => { setCurrentDevice(prev => ({ ...prev, [e.target.name]: e.target.value })); };

    // Mapare
    const handleAssign = async (deviceId) => {
        const val = selectedUserForDevice[deviceId];
        if (!val) return;
        if (val === "unassign") await axios.post(`/devices/${deviceId}/unassign`);
        else await axios.post(`/devices/${deviceId}/assign/${val}`);
        fetchData(); setSelectedUserForDevice(prev => ({...prev, [deviceId]: ""}));
    };

    const getStatusText = (deviceUsername) => {
        if (!deviceUsername) return <span className="text-warning fw-bold">Neatribuit</span>;
        const user = users.find(u => u.email === deviceUsername);
        return <span className="text-success fw-bold">Atribuit lui: {user ? user.name : deviceUsername}</span>;
    };

    const cardStyle = { border: "2px solid black", boxShadow: "5px 5px 5px black" };

    return (
        <div className="container mt-5">
            {/* Input ascuns pentru upload */}
            <input
                type="file"
                ref={fileInputRef}
                style={{ display: 'none' }}
                accept=".json"
                onChange={handleFileChange}
            />

            <div className="d-flex justify-content-between align-items-center mb-5 p-3 rounded" style={{backgroundColor: '#f8f9fa', border: '1px solid #dee2e6'}}>
                <h1 className="fw-normal m-0">Admin Dashboard</h1>
                <div className="d-flex gap-3">
                    <button className="btn btn-outline-primary px-3 fw-bold" onClick={handleExportData}>
                        💾 Backup Database
                    </button>
                    {/* Butonul care declanșează input-ul ascuns */}
                    <button className="btn btn-outline-warning px-3 fw-bold" onClick={() => fileInputRef.current.click()}>
                        📂 Restore Database
                    </button>
                    <button className="btn btn-danger px-4" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>

            <div className="row gx-5">
                <div className="col-md-6 mb-4">
                    <div className="card h-100" style={cardStyle}>
                        <div className="card-header bg-light d-flex justify-content-between py-3">
                            <h5 className="mb-0 text-secondary">Utilizatori</h5>
                            <button className="btn btn-success btn-sm px-3 fw-bold" onClick={handleCreateUser}>+ Adaugă</button>
                        </div>
                        <div className="list-group list-group-flush">
                            {users.map(u => (
                                <div key={u.id} className="list-group-item py-3">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <div><h6 className="mb-0 fw-bold">{u.name}</h6><small className="text-muted">{u.email} ({u.role})</small></div>
                                        <div className="btn-group">
                                            <button className="btn btn-outline-info btn-sm" onClick={() => handleViewClick(u)}>Vezi</button>
                                            <button className="btn btn-outline-warning btn-sm" onClick={() => handleEditClick(u)}>Edit</button>
                                            <button className="btn btn-outline-danger btn-sm" onClick={() => handleDeleteUser(u.id)}>Șterge</button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <div className="col-md-6 mb-4">
                    <div className="card h-100" style={cardStyle}>
                        <div className="card-header bg-light d-flex justify-content-between py-3">
                            <h5 className="mb-0 text-secondary">Dispozitive & Mapare</h5>
                            <button className="btn btn-success btn-sm px-3 fw-bold" onClick={() => setShowCreateDeviceModal(true)}>+ Adaugă</button>
                        </div>
                        <div className="card-body p-0">
                            {devices.map(d => (
                                <div key={d.id} className="p-3 border-bottom">
                                    <div className="d-flex justify-content-between mb-3">
                                        <div className="d-flex align-items-center gap-2"><span className="fw-bold">{d.name}</span><span className="badge bg-info text-white">{d.consumption} kW</span></div>
                                        <div className="btn-group">
                                            <button className="btn btn-outline-info btn-sm" onClick={() => handleDeviceViewClick(d)}>Vezi</button>
                                            <button className="btn btn-outline-warning btn-sm" onClick={() => handleDeviceEditClick(d)}>Edit</button>
                                            <button className="btn btn-outline-danger btn-sm" onClick={() => handleDeleteDevice(d.id)}>Șterge</button>
                                        </div>
                                    </div>
                                    <div className="d-flex gap-2 align-items-center mb-2">
                                        <select className="form-select form-select-sm" value={selectedUserForDevice[d.id] || ""} onChange={e => setSelectedUserForDevice({...selectedUserForDevice, [d.id]: e.target.value})}>
                                            <option value="">Schimbă atribuirea...</option>
                                            <option value="unassign" className="text-danger fw-bold">-- Neatribuit --</option>
                                            {users.filter(u => u.role === 'USER' || u.role === 'CLIENT').map(u => <option key={u.id} value={u.email}>{u.name}</option>)}
                                        </select>
                                        <button className="btn btn-primary btn-sm px-3" onClick={() => handleAssign(d.id)}>Save</button>
                                    </div>
                                    <div className="small text-muted">{getStatusText(d.username)}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            {/* --- SECȚIUNE NOUĂ: CHAT SUPPORT --- */}
            <div className="mt-5 mb-5 p-3" style={{ background: '#f8f9fa', borderRadius: '10px', border: '1px solid #ddd' }}>
                <h3 className="mb-3">🛠️ Support Chat Panel</h3>
                <div style={{ display: 'flex', height: '400px', gap: '20px' }}>
                    {/* Lista Conversații */}
                    <div style={{ width: '30%', background: 'white', borderRadius: '5px', border: '1px solid #ccc', overflowY: 'auto' }}>
                        <h5 className="p-3 border-bottom bg-light m-0">Conversații Active</h5>
                        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                            {chatUsers.length === 0 && <li className="p-3 text-muted">Niciun mesaj recent.</li>}
                            {chatUsers.map(u => (
                                <li key={u}
                                    onClick={() => selectUserToChat(u)}
                                    style={{
                                        padding: '15px',
                                        cursor: 'pointer',
                                        backgroundColor: selectedChatUser === u ? '#e9ecef' : 'transparent',
                                        borderBottom: '1px solid #eee',
                                        fontWeight: selectedChatUser === u ? 'bold' : 'normal'
                                    }}>
                                    👤 {u}
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Fereastra Chat */}
                    <div style={{ width: '70%', background: 'white', borderRadius: '5px', border: '1px solid #ccc', display: 'flex', flexDirection: 'column' }}>
                        {selectedChatUser ? (
                            <>
                                <div className="p-3 border-bottom bg-light">
                                    <strong>Chat cu: {selectedChatUser}</strong>
                                </div>
                                <div style={{ flex: 1, padding: '20px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    {adminMessages.map((msg, idx) => {
                                        const isAdmin = msg.senderId === 'admin' || msg.senderId === currentUser.id;
                                        return (
                                            <div key={idx} style={{
                                                alignSelf: isAdmin ? 'flex-end' : 'flex-start',
                                                backgroundColor: isAdmin ? '#0d6efd' : '#f1f0f0',
                                                color: isAdmin ? 'white' : 'black',
                                                padding: '8px 15px',
                                                borderRadius: '20px',
                                                maxWidth: '70%'
                                            }}>
                                                {msg.content}
                                            </div>
                                        );
                                    })}
                                    <div ref={messagesEndRef} />
                                </div>
                                <div className="p-3 border-top d-flex">
                                    <Form.Control
                                        type="text"
                                        placeholder="Răspunde..."
                                        value={adminInput}
                                        onChange={e => setAdminInput(e.target.value)}
                                        onKeyDown={e => e.key === 'Enter' && sendAdminMessage()}
                                        style={{ borderRadius: '20px' }}
                                    />
                                    <Button className="ms-2" style={{ borderRadius: '20px' }} onClick={sendAdminMessage}>Trimite</Button>
                                </div>
                            </>
                        ) : (
                            <div className="d-flex justify-content-center align-items-center h-100 text-muted">
                                Selectează un utilizator din stânga pentru a vedea conversația.
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* MODALELE UTILIZATORI */}
            <Modal show={showViewModal} onHide={() => setShowViewModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Detalii</Modal.Title></Modal.Header>
                <Modal.Body><p><strong>Nume:</strong> {currentUser.name}</p><p><strong>Email:</strong> {currentUser.email}</p><p><strong>Rol:</strong> {currentUser.role}</p></Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowViewModal(false)}>Închide</Button></Modal.Footer>
            </Modal>
            <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Editare</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3"><Form.Label>Nume</Form.Label><Form.Control name="name" value={currentUser.name} onChange={handleUserChange} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Email</Form.Label><Form.Control name="email" value={currentUser.email} onChange={handleUserChange} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Rol</Form.Label><Form.Select name="role" value={currentUser.role} onChange={handleUserChange}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></Form.Select></Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowEditModal(false)}>Anulează</Button><Button variant="primary" onClick={handleSaveUserChanges}>Salvează</Button></Modal.Footer>
            </Modal>

            {/* MODALELE DISPOZITIVE */}
            <Modal show={showCreateDeviceModal} onHide={() => setShowCreateDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Nou Dispozitiv</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3"><Form.Label>Nume</Form.Label><Form.Control placeholder="Nume" value={newDevice.name} onChange={e => setNewDevice({...newDevice, name: e.target.value})} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Consum</Form.Label><Form.Control type="number" value={newDevice.consumption} onChange={e => setNewDevice({...newDevice, consumption: e.target.value})} /></Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowCreateDeviceModal(false)}>Anulează</Button><Button variant="success" onClick={handleSaveNewDevice}>Creează</Button></Modal.Footer>
            </Modal>
            <Modal show={showViewDeviceModal} onHide={() => setShowViewDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Detalii</Modal.Title></Modal.Header>
                <Modal.Body><p><strong>Nume:</strong> {currentDevice.name}</p><p><strong>Consum:</strong> {currentDevice.consumption} kW</p><p><strong>Status:</strong> {currentDevice.active ? 'Activ' : 'Inactiv'}</p></Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowViewDeviceModal(false)}>Închide</Button></Modal.Footer>
            </Modal>
            <Modal show={showEditDeviceModal} onHide={() => setShowEditDeviceModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Editare Dispozitiv</Modal.Title></Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3"><Form.Label>Nume</Form.Label><Form.Control name="name" value={currentDevice.name} onChange={handleDeviceEditChange} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Consum</Form.Label><Form.Control name="consumption" type="number" value={currentDevice.consumption} onChange={handleDeviceEditChange} /></Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowEditDeviceModal(false)}>Anulează</Button><Button variant="primary" onClick={handleSaveDeviceChanges}>Salvează</Button></Modal.Footer>
            </Modal>
        </div>
    );
}