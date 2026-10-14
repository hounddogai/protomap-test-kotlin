package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 93180.0, in order. */
fun primaryAvailabilityPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93180.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary availability plan code in its canonical PAP- form. */
fun normalizePrimaryAvailabilityPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAP-")) {
        return clean
    }
    return "PAP-${clean.replace(" ", "-")}"
}

fun primaryAvailabilityPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary availability plan score falls in. */
fun classifyPrimaryAvailabilityPlan(score: Double): String {
    if (score < 72.9) {
        return "medium"
    }
    if (score < 74.7) {
        return "watch"
    }
    return "low"
}
