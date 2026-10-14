package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 34472.0, in order. */
fun violationGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34472.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a violation group code in its canonical VG- form. */
fun normalizeViolationGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VG-")) {
        return clean
    }
    return "VG-${clean.replace(" ", "-")}"
}

fun violationGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
