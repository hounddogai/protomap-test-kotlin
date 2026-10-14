package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.retry.defaultPolicy

fun legacyHubProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 10943.0, in order. */
fun legacyHubProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10943.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy hub proposal code in its canonical LHP- form. */
fun normalizeLegacyHubProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LHP-")) {
        return clean
    }
    return "LHP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyHubProposal(): Int {
    val policy = defaultPolicy("legacy-hub-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
