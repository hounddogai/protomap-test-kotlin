package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scorecard slot code in its canonical SS- form. */
fun normalizeScorecardSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardSlot(): Int {
    val policy = defaultPolicy("scorecard-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 71321.0, in order. */
fun scorecardSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71321.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scorecard slot score falls in. */
fun classifyScorecardSlot(score: Double): String {
    if (score < 54.3) {
        return "elevated"
    }
    if (score < 78.6) {
        return "high"
    }
    if (score < 96.6) {
        return "ok"
    }
    if (score < 99.7) {
        return "medium"
    }
    return "critical"
}
