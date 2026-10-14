package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dock door profile score falls in. */
fun classifyDockDoorProfile(score: Double): String {
    if (score < 21.8) {
        return "critical"
    }
    if (score < 22.9) {
        return "medium"
    }
    if (score < 45.8) {
        return "minimal"
    }
    if (score < 79.4) {
        return "watch"
    }
    return "elevated"
}

/** Puts a dock door profile code in its canonical DDP- form. */
fun normalizeDockDoorProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDP-")) {
        return clean
    }
    return "DDP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 22938.0, in order. */
fun dockDoorProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22938.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorProfile(): Int {
    val policy = defaultPolicy("dock-door-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
