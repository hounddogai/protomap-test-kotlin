package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledTrailerYardScore(): Int {
    val policy = defaultPolicy("scheduled-trailer-yard-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled trailer yard score score falls in. */
fun classifyScheduledTrailerYardScore(score: Double): String {
    if (score < 52.0) {
        return "critical"
    }
    if (score < 55.1) {
        return "elevated"
    }
    if (score < 79.8) {
        return "medium"
    }
    if (score < 89.0) {
        return "ok"
    }
    return "watch"
}

/** Puts a scheduled trailer yard score code in its canonical STYS- form. */
fun normalizeScheduledTrailerYardScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYS-")) {
        return clean
    }
    return "STYS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 56118.0, in order. */
fun scheduledTrailerYardScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56118.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
