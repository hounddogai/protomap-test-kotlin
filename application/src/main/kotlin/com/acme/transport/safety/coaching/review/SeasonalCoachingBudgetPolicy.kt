package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.retry.defaultPolicy

fun seasonalCoachingBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalCoachingBudget(): Int {
    val policy = defaultPolicy("seasonal-coaching-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal coaching budget code in its canonical SCB- form. */
fun normalizeSeasonalCoachingBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCB-")) {
        return clean
    }
    return "SCB-${clean.replace(" ", "-")}"
}
