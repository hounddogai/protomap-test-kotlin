package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 6778.0, in order. */
fun globalTenderMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6778.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalTenderMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global tender milestone code in its canonical GTM- form. */
fun normalizeGlobalTenderMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTM-")) {
        return clean
    }
    return "GTM-${clean.replace(" ", "-")}"
}
