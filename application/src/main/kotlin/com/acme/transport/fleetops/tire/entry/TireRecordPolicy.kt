package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tireRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 83796.0, in order. */
fun tireRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83796.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tire record code in its canonical TR- form. */
fun normalizeTireRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

fun tireRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
