package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.retry.defaultPolicy

fun defaultSortationRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultSortationRecord(): Int {
    val policy = defaultPolicy("default-sortation-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a default sortation record code in its canonical DSR- form. */
fun normalizeDefaultSortationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DSR-")) {
        return clean
    }
    return "DSR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 99773.0, in order. */
fun defaultSortationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99773.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
