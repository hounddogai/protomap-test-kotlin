package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a batched inspection checkpoint score falls in. */
fun classifyBatchedInspectionCheckpoint(score: Double): String {
    if (score < 4.2) {
        return "minimal"
    }
    if (score < 8.1) {
        return "watch"
    }
    if (score < 16.6) {
        return "medium"
    }
    if (score < 41.8) {
        return "low"
    }
    return "elevated"
}

/** Puts a batched inspection checkpoint code in its canonical BIC- form. */
fun normalizeBatchedInspectionCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIC-")) {
        return clean
    }
    return "BIC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedInspectionCheckpoint(): Int {
    val policy = defaultPolicy("batched-inspection-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
