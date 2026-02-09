import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// =====================
// ENV (run-time config)
// =====================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EVENT_KEY = __ENV.EVENT_KEY || 'EVENT_1';
const STRATEGY = __ENV.STRATEGY || 'optimistic';

// VU / duration (CLI로도 덮을 수 있음)
const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || '10s';

// (옵션) 너무 빡세면 약간 쉬게
const SLEEP_MS = Number(__ENV.SLEEP_MS || 0);

// =====================
// k6 options
// =====================
export const options = {
    vus: VUS,
    duration: DURATION,
    discardResponseBodies: true,
};

// =====================
// Metrics: status distribution
// =====================
const status200 = new Counter('status_200');
const status409 = new Counter('status_409');
const status503 = new Counter('status_503');
const statusOther = new Counter('status_other');

// =====================
// Setup: reset once
// =====================
export function setup() {
    const res = http.post(`${BASE_URL}/coupons/reset/${EVENT_KEY}?resetRedis=true`);
    check(res, { 'reset success (200)': (r) => r.status === 200 });
}

// =====================
// Default: issue loop
// =====================
export default function () {
    const userId = __VU * 100000 + __ITER;

    const params = {
        headers: { 'Content-Type': 'application/json' },

        // ✅ Treat 200/409/503 as "expected" so http_req_failed becomes meaningful
        expectedStatuses: [200, 409, 503],
    };

    const res = http.post(
        `${BASE_URL}/coupons/issue/${STRATEGY}/${EVENT_KEY}`,
        JSON.stringify({ userId }),
        params
    );

    // status counters
    if (res.status === 200) status200.add(1);
    else if (res.status === 409) status409.add(1);
    else if (res.status === 503) status503.add(1);
    else statusOther.add(1);

    check(res, {
        '200/409/503': (r) => r.status === 200 || r.status === 409 || r.status === 503,
    });

    if (SLEEP_MS > 0) sleep(SLEEP_MS / 1000);
}
