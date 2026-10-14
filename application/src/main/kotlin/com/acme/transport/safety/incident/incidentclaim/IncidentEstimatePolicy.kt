package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 63074.0, in order. */
fun incidentEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63074.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun incidentEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a incident estimate code in its canonical IE- form. */
fun normalizeIncidentEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IE-")) {
        return clean
    }
    return "IE-${clean.replace(" ", "-")}"
}
