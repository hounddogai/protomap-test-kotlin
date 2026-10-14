package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a global capacity schedule code in its canonical GCS- form. */
fun normalizeGlobalCapacityScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCS-")) {
        return clean
    }
    return "GCS-${clean.replace(" ", "-")}"
}

fun globalCapacityScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalCapacityScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 65235.0, in order. */
fun globalCapacityScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65235.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
