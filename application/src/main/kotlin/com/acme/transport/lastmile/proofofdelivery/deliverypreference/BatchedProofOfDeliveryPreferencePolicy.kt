package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedProofOfDeliveryPreference(): Int {
    val policy = defaultPolicy("batched-proof-of-delivery-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched proof of delivery preference score falls in. */
fun classifyBatchedProofOfDeliveryPreference(score: Double): String {
    if (score < 8.7) {
        return "medium"
    }
    if (score < 32.4) {
        return "minimal"
    }
    if (score < 35.7) {
        return "watch"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 75492.0, in order. */
fun batchedProofOfDeliveryPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75492.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched proof of delivery preference code in its canonical BPODP- form. */
fun normalizeBatchedProofOfDeliveryPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPODP-")) {
        return clean
    }
    return "BPODP-${clean.replace(" ", "-")}"
}
