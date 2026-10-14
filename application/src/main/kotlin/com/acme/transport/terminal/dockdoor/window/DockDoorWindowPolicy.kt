package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorWindow(): Int {
    val policy = defaultPolicy("dock-door-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a dock door window code in its canonical DDW- form. */
fun normalizeDockDoorWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDW-")) {
        return clean
    }
    return "DDW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 35253.0, in order. */
fun dockDoorWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35253.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dockDoorWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
