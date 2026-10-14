package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.numbers.roundTo

/** Names the band a clearance quota score falls in. */
fun classifyClearanceQuota(score: Double): String {
    if (score < 54.6) {
        return "low"
    }
    if (score < 67.3) {
        return "critical"
    }
    if (score < 85.8) {
        return "minimal"
    }
    if (score < 89.0) {
        return "ok"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 37696.0, in order. */
fun clearanceQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37696.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun clearanceQuotaBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a clearance quota code in its canonical CQ- form. */
fun normalizeClearanceQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CQ-")) {
        return clean
    }
    return "CQ-${clean.replace(" ", "-")}"
}
