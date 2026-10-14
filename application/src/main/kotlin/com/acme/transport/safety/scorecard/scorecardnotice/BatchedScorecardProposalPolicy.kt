package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedScorecardProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched scorecard proposal code in its canonical BSP- form. */
fun normalizeBatchedScorecardProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSP-")) {
        return clean
    }
    return "BSP-${clean.replace(" ", "-")}"
}

/** Names the band a batched scorecard proposal score falls in. */
fun classifyBatchedScorecardProposal(score: Double): String {
    if (score < 13.6) {
        return "critical"
    }
    if (score < 35.8) {
        return "high"
    }
    return "ok"
}
