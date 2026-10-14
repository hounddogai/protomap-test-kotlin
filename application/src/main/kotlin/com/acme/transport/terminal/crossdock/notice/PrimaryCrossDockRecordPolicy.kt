package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 32753.0, in order. */
fun primaryCrossDockRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32753.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary cross dock record code in its canonical PCDR- form. */
fun normalizePrimaryCrossDockRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCDR-")) {
        return clean
    }
    return "PCDR-${clean.replace(" ", "-")}"
}

fun primaryCrossDockRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCrossDockRecord(): Int {
    val policy = defaultPolicy("primary-cross-dock-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
