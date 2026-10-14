package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun servicePlanExceptionBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun servicePlanExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 91519.0, in order. */
fun servicePlanExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91519.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a service plan exception code in its canonical SPE- form. */
fun normalizeServicePlanExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPE-")) {
        return clean
    }
    return "SPE-${clean.replace(" ", "-")}"
}
