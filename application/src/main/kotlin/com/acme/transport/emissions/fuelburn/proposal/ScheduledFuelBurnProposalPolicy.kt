package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledFuelBurnProposal(): Int {
    val policy = defaultPolicy("scheduled-fuel-burn-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled fuel burn proposal code in its canonical SFBP- form. */
fun normalizeScheduledFuelBurnProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFBP-")) {
        return clean
    }
    return "SFBP-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled fuel burn proposal score falls in. */
fun classifyScheduledFuelBurnProposal(score: Double): String {
    if (score < 5.0) {
        return "high"
    }
    if (score < 81.8) {
        return "ok"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 58421.0, in order. */
fun scheduledFuelBurnProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58421.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
