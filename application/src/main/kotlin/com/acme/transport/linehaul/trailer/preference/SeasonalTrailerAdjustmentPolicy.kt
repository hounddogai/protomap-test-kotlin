package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a seasonal trailer adjustment code in its canonical STA- form. */
fun normalizeSeasonalTrailerAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STA-")) {
        return clean
    }
    return "STA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTrailerAdjustment(): Int {
    val policy = defaultPolicy("seasonal-trailer-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal trailer adjustment score falls in. */
fun classifySeasonalTrailerAdjustment(score: Double): String {
    if (score < 23.4) {
        return "medium"
    }
    if (score < 31.8) {
        return "high"
    }
    if (score < 51.5) {
        return "low"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 77171.0, in order. */
fun seasonalTrailerAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77171.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
