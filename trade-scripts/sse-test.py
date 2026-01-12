import requests
import threading
import time

def subscribe_to_stream(client_id):
    url = "http://localhost:8085/api/trades/prices/stream"
    response = requests.get(url, stream=True)
    
    print(f"Client {client_id} connected")
    
    for line in response.iter_lines():
        if line:
            print(f"Client {client_id}: {line.decode()}")

# Start 10 concurrent SSE connections
threads = []
for i in range(10):
    t = threading.Thread(target=subscribe_to_stream, args=(i,))
    t.daemon = True
    t.start()
    threads.append(t)
    time.sleep(0.1)

# Run for 60 seconds
time.sleep(60)