package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.retry.defaultPolicy

fun regionalRestBreakAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a regional rest break adjustment code in its canonical RRBA- form. */
fun normalizeRegionalRestBreakAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBA-")) {
        return clean
    }
    return "RRBA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37983.0, in order. */
fun regionalRestBreakAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37983.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalRestBreakAdjustment(): Int {
    val policy = defaultPolicy("regional-rest-break-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
