package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defectSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a defect snapshot code in its canonical DS- form. */
fun normalizeDefectSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 3937.0, in order. */
fun defectSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3937.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a defect snapshot score falls in. */
fun classifyDefectSnapshot(score: Double): String {
    if (score < 13.7) {
        return "high"
    }
    if (score < 18.4) {
        return "critical"
    }
    if (score < 56.5) {
        return "low"
    }
    return "minimal"
}
