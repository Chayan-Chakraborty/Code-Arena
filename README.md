# Code Arena

A LeetCode-style coding platform with real Java code execution via Judge0.

## Stack
- **Backend**: Spring Boot 3, Spring Security + JWT, JPA, H2
- **Frontend**: React + React Router + Tailwind + Monaco Editor
- **Compiler**: Judge0 CE (RapidAPI hosted by default)

## Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on http://localhost:8080.

### Judge0 configuration

Sign up at https://rapidapi.com/judge0-official/api/judge0-ce/ and grab a free API key,
then set:

```bash
export JUDGE0_API_KEY=<your-rapidapi-key>
# Optional overrides:
# export JUDGE0_URL=https://judge0-ce.p.rapidapi.com
# export JUDGE0_HOST=judge0-ce.p.rapidapi.com
```

For a self-hosted Judge0 (no key needed), set `JUDGE0_URL` to your instance and use any
non-empty `JUDGE0_API_KEY` (the header is sent but ignored by self-hosted instances).

## Frontend

```bash
cd frontend
npm install
npm start
```

Opens http://localhost:3000.

## Features

- **Problems list** filtered by topic (`/problems?topic=Arrays`)
- **Problem IDE page**: split view with description + Monaco editor
- **Run** against sample test cases (visible)
- **Submit** against hidden test cases — returns Accepted / Wrong Answer / TLE / Runtime Error
- **Dashboard**: total solved, easy/medium/hard counts, topic-wise progress bars
- **Auth**: signup/login with JWT

## Seeded Problems

1. Two Sum (Easy, Arrays)
2. Maximum Subarray (Medium, Arrays)
3. Move Zeroes (Easy, Arrays)
4. Contains Duplicate (Easy, Arrays)
5. Best Time to Buy and Sell Stock (Easy, Arrays)

Each ships with 2 sample + 3 hidden test cases.

## I/O Contract

Each problem reads stdin and prints to stdout. The exact input format is documented
in each problem description. Starter code already contains the Scanner boilerplate.
