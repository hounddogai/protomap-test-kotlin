package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionProposal(): Int {
    val policy = defaultPolicy("inspection-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a inspection proposal score falls in. */
fun classifyInspectionProposal(score: Double): String {
    if (score < 44.0) {
        return "critical"
    }
    if (score < 65.4) {
        return "medium"
    }
    if (score < 68.5) {
        return "ok"
    }
    if (score < 73.9) {
        return "high"
    }
    return "minimal"
}

/** Puts a inspection proposal code in its canonical IP- form. */
fun normalizeInspectionProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IP-")) {
        return clean
    }
    return "IP-${clean.replace(" ", "-")}"
}
