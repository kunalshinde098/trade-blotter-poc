import requests
import json
import random
from datetime import datetime, timedelta

ES_URL = "http://localhost:9200"
INDEX = "trades"

BOOKS = ["EMEA_RATES", "US_RATES", "ASIA_RATES", "EMEA_FX", "US_FX"]
INSTRUMENTS = ["EUR_IRS_10Y", "USD_IRS_5Y", "GBP_IRS_7Y", "JPY_IRS_3Y"]
TRADERS = ["john.smith", "jane.doe", "bob.wilson", "alice.johnson"]
COUNTERPARTIES = ["BANK_ABC", "BANK_XYZ", "BANK_123", "BROKER_A"]
STATUSES = ["ACTIVE", "PENDING", "SETTLED", "CANCELLED"]

def generate_trade(index):
    trade_id = f"TRD{index:06d}"
    trade_date = (datetime.now() - timedelta(days=random.randint(0, 365))).strftime("%Y-%m-%d")
    
    notional = random.randint(1000000, 10000000)
    pnl = random.uniform(-100000, 100000)
    mtm = pnl * 1.05
    
    additional_fields = {f"field{i}": f"value_{random.randint(0, 1000)}" 
                        for i in range(1, 381)}
    
    return {
        "tradeId": trade_id,
        "book": random.choice(BOOKS),
        "tradeDate": trade_date,
        "instrument": random.choice(INSTRUMENTS),
        "trader": random.choice(TRADERS),
        "counterparty": random.choice(COUNTERPARTIES),
        "notional": notional,
        "pnl": round(pnl, 2),
        "mtm": round(mtm, 2),
        "currency": "USD",
        "tradeType": "IRS",
        "status": random.choice(STATUSES),
        "settlementDate": trade_date,
        "maturityDate": trade_date,
        "fixedRate": round(random.uniform(1.5, 4.5), 2),
        "floatingRate": round(random.uniform(1.5, 4.5), 2),
        "delta": round(random.uniform(-1000, 1000), 2),
        "gamma": round(random.uniform(-50, 50), 2),
        "vega": round(random.uniform(-500, 500), 2),
        "theta": round(random.uniform(-25, 25), 2),
        "additionalFields": additional_fields
    }

def bulk_insert(trades):
    bulk_data = []
    for trade in trades:
        bulk_data.append(json.dumps({"index": {"_index": INDEX, "_id": trade["tradeId"]}}))
        bulk_data.append(json.dumps(trade))
    
    bulk_body = "\n".join(bulk_data) + "\n"
    
    response = requests.post(
        f"{ES_URL}/_bulk",
        headers={"Content-Type": "application/x-ndjson"},
        data=bulk_body
    )
    
    return response.json()

def ingest_trades(total_count, batch_size=1000):
    print(f"Ingesting {total_count} trades in batches of {batch_size}...")
    
    for start in range(1, total_count + 1, batch_size):
        end = min(start + batch_size, total_count + 1)
        trades = [generate_trade(i) for i in range(start, end)]
        
        result = bulk_insert(trades)
        
        if result.get("errors"):
            print(f"Errors in batch {start}-{end}")
        else:
            print(f"Inserted trades {start}-{end}")
    
    # Refresh index
    requests.post(f"{ES_URL}/{INDEX}/_refresh")
    print("Data ingestion complete!")

if __name__ == "__main__":
    import sys
    count = int(sys.argv[1]) if len(sys.argv) > 1 else 10000
    ingest_trades(count)