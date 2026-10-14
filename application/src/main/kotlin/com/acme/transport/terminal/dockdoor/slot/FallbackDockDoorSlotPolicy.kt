package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 14433.0, in order. */
fun fallbackDockDoorSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14433.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackDockDoorSlotSpan(start: Int, end: Int): Int {
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
fun probeFallbackDockDoorSlot(): Int {
    val policy = defaultPolicy("fallback-dock-door-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a fallback dock door slot code in its canonical FDDS- form. */
fun normalizeFallbackDockDoorSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDDS-")) {
        return clean
    }
    return "FDDS-${clean.replace(" ", "-")}"
}
