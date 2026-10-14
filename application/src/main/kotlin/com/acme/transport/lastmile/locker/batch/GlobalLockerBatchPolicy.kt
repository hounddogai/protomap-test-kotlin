package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalLockerBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global locker batch code in its canonical GLB- form. */
fun normalizeGlobalLockerBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GLB-")) {
        return clean
    }
    return "GLB-${clean.replace(" ", "-")}"
}

/** Names the band a global locker batch score falls in. */
fun classifyGlobalLockerBatch(score: Double): String {
    if (score < 23.4) {
        return "medium"
    }
    if (score < 62.5) {
        return "critical"
    }
    if (score < 65.0) {
        return "low"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 80712.0, in order. */
fun globalLockerBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80712.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
