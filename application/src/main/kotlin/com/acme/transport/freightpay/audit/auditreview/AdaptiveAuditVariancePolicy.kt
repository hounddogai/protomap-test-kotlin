package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a adaptive audit variance code in its canonical AAV- form. */
fun normalizeAdaptiveAuditVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAV-")) {
        return clean
    }
    return "AAV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 96772.0, in order. */
fun adaptiveAuditVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96772.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveAuditVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive audit variance score falls in. */
fun classifyAdaptiveAuditVariance(score: Double): String {
    if (score < 11.6) {
        return "high"
    }
    if (score < 41.1) {
        return "critical"
    }
    if (score < 44.3) {
        return "medium"
    }
    if (score < 76.4) {
        return "ok"
    }
    return "low"
}
