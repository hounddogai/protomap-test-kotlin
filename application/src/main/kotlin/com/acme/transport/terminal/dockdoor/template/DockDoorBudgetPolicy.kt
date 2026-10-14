package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dockDoorBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dock door budget code in its canonical DDB- form. */
fun normalizeDockDoorBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDB-")) {
        return clean
    }
    return "DDB-${clean.replace(" ", "-")}"
}
