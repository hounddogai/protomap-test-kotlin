package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 6177.0, in order. */
fun cappedLanePlanPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6177.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped lane plan preference score falls in. */
fun classifyCappedLanePlanPreference(score: Double): String {
    if (score < 23.3) {
        return "medium"
    }
    if (score < 39.9) {
        return "high"
    }
    return "ok"
}

/** Puts a capped lane plan preference code in its canonical CLPP- form. */
fun normalizeCappedLanePlanPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CLPP-")) {
        return clean
    }
    return "CLPP-${clean.replace(" ", "-")}"
}

fun cappedLanePlanPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
