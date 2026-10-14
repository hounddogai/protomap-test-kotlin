package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedIncidentProposal(): Int {
    val policy = defaultPolicy("batched-incident-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched incident proposal code in its canonical BIP- form. */
fun normalizeBatchedIncidentProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIP-")) {
        return clean
    }
    return "BIP-${clean.replace(" ", "-")}"
}

/** Names the band a batched incident proposal score falls in. */
fun classifyBatchedIncidentProposal(score: Double): String {
    if (score < 1.7) {
        return "elevated"
    }
    if (score < 4.3) {
        return "critical"
    }
    if (score < 64.2) {
        return "medium"
    }
    return "minimal"
}

fun batchedIncidentProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
