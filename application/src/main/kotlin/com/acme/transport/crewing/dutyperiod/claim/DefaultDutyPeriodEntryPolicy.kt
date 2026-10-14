package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default duty period entry code in its canonical DDPE- form. */
fun normalizeDefaultDutyPeriodEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDPE-")) {
        return clean
    }
    return "DDPE-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDutyPeriodEntry(): Int {
    val policy = defaultPolicy("default-duty-period-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 6603.0, in order. */
fun defaultDutyPeriodEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6603.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultDutyPeriodEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
