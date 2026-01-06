import pika
import json
import random
import sys
import time
from datetime import datetime

# --- CONFIGURARE ---
QUEUE_NAME = 'device_queue'
CONFIG_FILE = 'sensor_config.properties'
RABBIT_HOST = 'localhost' # Se conectează la Docker-ul care rulează pe portul 5672

def read_device_id():
    """Citește device_id din fișierul .properties"""
    try:
        with open(CONFIG_FILE, 'r') as f:
            for line in f:
                if line.strip().startswith('device_id='):
                    return line.split('=')[1].strip()
    except FileNotFoundError:
        print(f"Eroare: Fișierul {CONFIG_FILE} nu a fost găsit.")
        sys.exit(1)
    return None

def generate_consumption():
    """Generează un consum realist bazat pe ora curentă"""
    hour = datetime.now().hour
    base_value = 0.0
    
    # Logică similară cu cea din Java
    if 0 <= hour < 6:
        base_value = 0.3  # Noaptea
    elif 6 <= hour < 9:
        base_value = 1.0  # Dimineața
    elif 9 <= hour < 17:
        base_value = 0.8  # Ziua
    elif 17 <= hour < 22:
        base_value = 2.5  # Seara (Vârf)
    else:
        base_value = 0.5  # Noaptea târziu

    # Adăugăm fluctuații random (+/-)
    fluctuation = random.uniform(-0.1, 0.1)
    final_value = base_value + fluctuation
    
    # Rotunjim la 2 zecimale și ne asigurăm că e pozitiv
    return max(0, round(final_value, 2))

def main():
    print(">>> Python Simulator Started...")
    
    device_id = read_device_id()
    if not device_id:
        print("Nu s-a găsit device_id valid.")
        return

    print(f">>> Simulating for Device ID: {device_id}")

    try:
        # Conectare la RabbitMQ
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBIT_HOST))
        channel = connection.channel()

        # Declararea cozii (durable=True ca în Java)
        channel.queue_declare(queue=QUEUE_NAME, durable=True)

        while True:
            # 1. Calcul consum
            measurement = generate_consumption()
            
            # 2. Generare Timestamp formatat
            timestamp = int(time.time() * 1000)

            # 3. Creare JSON (Python dicts sunt ordonate în versiunile noi, deci ordinea se păstrează)
            message_data = {
                "timestamp": timestamp,
                "device_id": device_id,
                "measurement_value": measurement
            }
            
            json_message = json.dumps(message_data)

            # 4. Trimitere mesaj
            channel.basic_publish(
                exchange='',
                routing_key=QUEUE_NAME,
                body=json_message
            )
            
            print(f" [x] Sent: {json_message}")

            # 5. Pauză (3 secunde pentru test, 600 pentru cerința de 10 min)
            time.sleep(600)

    except pika.exceptions.AMQPConnectionError:
        print("Eroare: Nu m-am putut conecta la RabbitMQ. Asigură-te că Docker rulează!")
    except KeyboardInterrupt:
        print("\nSimulator oprit manual.")
        if 'connection' in locals() and connection.is_open:
            connection.close()

if __name__ == '__main__':
    main()