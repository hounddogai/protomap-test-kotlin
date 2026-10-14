package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.numbers.roundTo

fun tieredDockDoorTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a tiered dock door target score falls in. */
fun classifyTieredDockDoorTarget(score: Double): String {
    if (score < 18.6) {
        return "minimal"
    }
    if (score < 21.8) {
        return "low"
    }
    if (score < 86.8) {
        return "ok"
    }
    if (score < 94.6) {
        return "watch"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 38118.0, in order. */
fun tieredDockDoorTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38118.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered dock door target code in its canonical TDDT- form. */
fun normalizeTieredDockDoorTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDDT-")) {
        return clean
    }
    return "TDDT-${clean.replace(" ", "-")}"
}
