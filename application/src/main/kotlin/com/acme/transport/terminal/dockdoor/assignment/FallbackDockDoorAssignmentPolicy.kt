package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fallback dock door assignment code in its canonical FDDA- form. */
fun normalizeFallbackDockDoorAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDDA-")) {
        return clean
    }
    return "FDDA-${clean.replace(" ", "-")}"
}

fun fallbackDockDoorAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 94484.0, in order. */
fun fallbackDockDoorAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94484.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackDockDoorAssignment(): Int {
    val policy = defaultPolicy("fallback-dock-door-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
