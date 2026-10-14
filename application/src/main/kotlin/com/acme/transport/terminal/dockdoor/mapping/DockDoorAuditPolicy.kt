package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dockDoorAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dock door audit code in its canonical DDA- form. */
fun normalizeDockDoorAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDA-")) {
        return clean
    }
    return "DDA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 41560.0, in order. */
fun dockDoorAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41560.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
