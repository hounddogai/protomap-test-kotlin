package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a batched qualification milestone score falls in. */
fun classifyBatchedQualificationMilestone(score: Double): String {
    if (score < 36.2) {
        return "critical"
    }
    if (score < 57.3) {
        return "elevated"
    }
    return "ok"
}

fun batchedQualificationMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun batchedQualificationMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched qualification milestone code in its canonical BQM- form. */
fun normalizeBatchedQualificationMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BQM-")) {
        return clean
    }
    return "BQM-${clean.replace(" ", "-")}"
}
