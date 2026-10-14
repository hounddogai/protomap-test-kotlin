package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 82807.0, in order. */
fun batchedServicePlanSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82807.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched service plan summary score falls in. */
fun classifyBatchedServicePlanSummary(score: Double): String {
    if (score < 29.9) {
        return "high"
    }
    if (score < 86.8) {
        return "minimal"
    }
    return "watch"
}

fun batchedServicePlanSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched service plan summary code in its canonical BSPS- form. */
fun normalizeBatchedServicePlanSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSPS-")) {
        return clean
    }
    return "BSPS-${clean.replace(" ", "-")}"
}
