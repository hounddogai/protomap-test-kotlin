package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a defect digest score falls in. */
fun classifyDefectDigest(score: Double): String {
    if (score < 2.8) {
        return "medium"
    }
    if (score < 16.7) {
        return "elevated"
    }
    if (score < 59.3) {
        return "high"
    }
    if (score < 76.2) {
        return "minimal"
    }
    return "ok"
}

/** Puts a defect digest code in its canonical DD- form. */
fun normalizeDefectDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DD-")) {
        return clean
    }
    return "DD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 32598.0, in order. */
fun defectDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32598.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defectDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
