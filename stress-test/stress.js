import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// =====================================================
// Configuration
// =====================================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8085';

const ACCOUNTS = (__ENV.ACCOUNTS || '')
    .split(',')
    .map((a) => a.trim())
    .filter((a) => a.length > 0);

const AMOUNTS = [
    1,
    2,
    5,
    10,
    20,
    25,
    50,
    75,
    100
];

const RPS = Number(__ENV.RPS || 20);
const DURATION = __ENV.DURATION || '15m';

// =====================================================
// Custom metrics
// =====================================================

const transferErrors = new Rate('transfer_errors');
const transferLatency = new Trend('transfer_latency_ms');

// =====================================================
// k6 configuration
// =====================================================

export const options = {

    scenarios: {

        transfer_stress: {

            executor: 'ramping-arrival-rate',

            startRate: 1,

            timeUnit: '1s',

            preAllocatedVUs: 30,

            maxVUs: 100,

            stages: [

                // Gradually increase traffic
                {
                    duration: '30s',
                    target: RPS
                },

                // Keep the target traffic
                {
                    duration: DURATION,
                    target: RPS
                },

                // Gradually stop traffic
                {
                    duration: '30s',
                    target: 0
                }
            ],

            gracefulStop: '30s'
        }
    },

    thresholds: {

        // Less than 2% HTTP failures
        http_req_failed: [
            'rate<0.02'
        ],

        // Less than 2% transfer failures
        transfer_errors: [
            'rate<0.02'
        ],

        // 95% of requests must be below 2.5 seconds
        http_req_duration: [
            'p(95)<2500'
        ]
    }
};


// =====================================================
// Setup
// =====================================================

export function setup() {

    if (ACCOUNTS.length < 2) {

        throw new Error(
            'ACCOUNTS must contain at least 2 accounts'
        );
    }

    console.log(
        `Starting transfer stress test with ${ACCOUNTS.length} accounts`
    );

    console.log(
        `Target RPS: ${RPS}`
    );

    console.log(
        `Duration: ${DURATION}`
    );

    return {
        accounts: ACCOUNTS
    };
}


// =====================================================
// Main test
// =====================================================

export default function (data) {

    const accounts = data.accounts;

    const accountCount = accounts.length;

    // -------------------------------------------------
    // Select sender and receiver
    // -------------------------------------------------

    const fromIndex = (__ITER + __VU) % accountCount;

    const toIndex = (fromIndex + 1) % accountCount;

    const fromAccount = accounts[fromIndex];

    const toAccount = accounts[toIndex];


    // -------------------------------------------------
    // Select different amount
    // -------------------------------------------------

    const amountIndex =
        (__ITER + __VU) % AMOUNTS.length;

    const amount = AMOUNTS[amountIndex];


    // -------------------------------------------------
    // Create request
    // -------------------------------------------------

    const payload = JSON.stringify({

        fromAccountNumber: fromAccount,

        toAccountNumber: toAccount,

        amount: amount,

        description:
            `stress-${__VU}-${__ITER}-${Date.now()}`
    });


    // -------------------------------------------------
    // Send transfer
    // -------------------------------------------------

    const res = http.post(

        `${BASE_URL}/api/payments/transfer`,

        payload,

        {
            headers: {
                'Content-Type': 'application/json'
            },

            tags: {
                endpoint: 'transfer'
            }
        }
    );


    // -------------------------------------------------
    // Metrics
    // -------------------------------------------------

    transferLatency.add(
        res.timings.duration
    );

    const failed =
        res.status !== 200;

    transferErrors.add(failed);


    // -------------------------------------------------
    // Validate response
    // -------------------------------------------------

    check(res, {

        'transfer returns 200':
            (r) => r.status === 200,

        'response contains transferId':
            (r) => {

                try {

                    const transferId =
                        r.json('transferId');

                    return (
                        transferId !== null &&
                        transferId !== undefined
                    );

                } catch (e) {

                    return false;
                }
            }
    });


    // Small pause between iterations
    sleep(0.2);
}