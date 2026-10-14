package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun servicePlanOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun servicePlanOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 71908.0, in order. */
fun servicePlanOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71908.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a service plan override code in its canonical SPO- form. */
fun normalizeServicePlanOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPO-")) {
        return clean
    }
    return "SPO-${clean.replace(" ", "-")}"
}
