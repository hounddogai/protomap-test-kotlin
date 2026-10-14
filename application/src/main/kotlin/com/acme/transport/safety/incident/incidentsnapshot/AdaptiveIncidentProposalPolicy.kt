package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveIncidentProposal(): Int {
    val policy = defaultPolicy("adaptive-incident-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive incident proposal code in its canonical AIP- form. */
fun normalizeAdaptiveIncidentProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIP-")) {
        return clean
    }
    return "AIP-${clean.replace(" ", "-")}"
}

fun adaptiveIncidentProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
