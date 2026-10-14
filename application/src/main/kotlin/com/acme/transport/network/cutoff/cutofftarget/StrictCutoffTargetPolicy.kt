package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a strict cutoff target code in its canonical SCT- form. */
fun normalizeStrictCutoffTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCT-")) {
        return clean
    }
    return "SCT-${clean.replace(" ", "-")}"
}

fun strictCutoffTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 75724.0, in order. */
fun strictCutoffTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75724.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictCutoffTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
