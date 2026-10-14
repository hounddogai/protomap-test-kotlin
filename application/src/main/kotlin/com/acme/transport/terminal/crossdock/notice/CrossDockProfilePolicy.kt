package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 4031.0, in order. */
fun crossDockProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4031.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun crossDockProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun crossDockProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a cross dock profile code in its canonical CDP- form. */
fun normalizeCrossDockProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDP-")) {
        return clean
    }
    return "CDP-${clean.replace(" ", "-")}"
}
