package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 73974.0, in order. */
fun scheduledCoachingTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73974.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledCoachingTier(): Int {
    val policy = defaultPolicy("scheduled-coaching-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled coaching tier code in its canonical SCT- form. */
fun normalizeScheduledCoachingTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCT-")) {
        return clean
    }
    return "SCT-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled coaching tier score falls in. */
fun classifyScheduledCoachingTier(score: Double): String {
    if (score < 11.7) {
        return "watch"
    }
    if (score < 50.5) {
        return "minimal"
    }
    if (score < 96.2) {
        return "elevated"
    }
    return "low"
}
