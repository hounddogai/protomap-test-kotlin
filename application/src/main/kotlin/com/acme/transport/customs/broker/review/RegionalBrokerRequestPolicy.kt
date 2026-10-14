package com.acme.transport.customs.broker.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalBrokerRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional broker request score falls in. */
fun classifyRegionalBrokerRequest(score: Double): String {
    if (score < 4.1) {
        return "high"
    }
    if (score < 22.1) {
        return "watch"
    }
    if (score < 25.5) {
        return "medium"
    }
    if (score < 87.7) {
        return "low"
    }
    return "elevated"
}

/** Puts a regional broker request code in its canonical RBR- form. */
fun normalizeRegionalBrokerRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBR-")) {
        return clean
    }
    return "RBR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 50679.0, in order. */
fun regionalBrokerRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50679.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
