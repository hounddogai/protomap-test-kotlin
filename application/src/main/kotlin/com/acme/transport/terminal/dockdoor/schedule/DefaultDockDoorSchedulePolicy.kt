package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default dock door schedule code in its canonical DDDS- form. */
fun normalizeDefaultDockDoorScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDDS-")) {
        return clean
    }
    return "DDDS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDockDoorSchedule(): Int {
    val policy = defaultPolicy("default-dock-door-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 58778.0, in order. */
fun defaultDockDoorScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58778.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
