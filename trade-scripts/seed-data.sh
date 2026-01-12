# Seed 1,000 trades (run 10 times)
for i in {1..10}; do ./seed-data.sh; done

# Seed 10,000 trades (run 100 times in parallel)
for i in {1..100}; do ./seed-data.sh & done; wait

# Seed 100,000 trades (run 1000 times - takes ~20 minutes)
for i in {1..1000}; do 
  ./seed-data.sh & 
  if [ $((i % 50)) -eq 0 ]; then 
    wait
    echo "Seeded $((i * 100)) trades so far..."
  fi
done
wait