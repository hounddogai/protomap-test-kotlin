package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalDwellProposal(): Int {
    val policy = defaultPolicy("global-dwell-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global dwell proposal score falls in. */
fun classifyGlobalDwellProposal(score: Double): String {
    if (score < 5.7) {
        return "medium"
    }
    if (score < 8.2) {
        return "elevated"
    }
    if (score < 79.2) {
        return "high"
    }
    if (score < 82.7) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 62108.0, in order. */
fun globalDwellProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62108.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global dwell proposal code in its canonical GDP- form. */
fun normalizeGlobalDwellProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDP-")) {
        return clean
    }
    return "GDP-${clean.replace(" ", "-")}"
}
