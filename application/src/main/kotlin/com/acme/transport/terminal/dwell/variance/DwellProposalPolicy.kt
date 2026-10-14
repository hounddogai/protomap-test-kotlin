package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dwell proposal score falls in. */
fun classifyDwellProposal(score: Double): String {
    if (score < 6.6) {
        return "elevated"
    }
    if (score < 35.2) {
        return "low"
    }
    if (score < 79.9) {
        return "minimal"
    }
    return "ok"
}

/** Puts a dwell proposal code in its canonical DP- form. */
fun normalizeDwellProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 74356.0, in order. */
fun dwellProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74356.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellProposal(): Int {
    val policy = defaultPolicy("dwell-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
