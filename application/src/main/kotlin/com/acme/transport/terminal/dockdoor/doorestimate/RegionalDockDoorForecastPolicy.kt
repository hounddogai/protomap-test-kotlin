package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a regional dock door forecast code in its canonical RDDF- form. */
fun normalizeRegionalDockDoorForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDDF-")) {
        return clean
    }
    return "RDDF-${clean.replace(" ", "-")}"
}

fun regionalDockDoorForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 22656.0, in order. */
fun regionalDockDoorForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22656.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalDockDoorForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
