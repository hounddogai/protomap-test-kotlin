package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary proof of delivery proposal score falls in. */
fun classifyPrimaryProofOfDeliveryProposal(score: Double): String {
    if (score < 9.3) {
        return "low"
    }
    if (score < 24.0) {
        return "elevated"
    }
    if (score < 25.3) {
        return "minimal"
    }
    if (score < 97.3) {
        return "medium"
    }
    return "critical"
}

fun primaryProofOfDeliveryProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a primary proof of delivery proposal code in its canonical PPODP- form. */
fun normalizePrimaryProofOfDeliveryProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PPODP-")) {
        return clean
    }
    return "PPODP-${clean.replace(" ", "-")}"
}
