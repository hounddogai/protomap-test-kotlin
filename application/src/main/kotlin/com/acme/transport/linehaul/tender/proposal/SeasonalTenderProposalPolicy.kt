package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTenderProposal(): Int {
    val policy = defaultPolicy("seasonal-tender-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 20261.0, in order. */
fun seasonalTenderProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20261.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal tender proposal code in its canonical STP- form. */
fun normalizeSeasonalTenderProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STP-")) {
        return clean
    }
    return "STP-${clean.replace(" ", "-")}"
}
