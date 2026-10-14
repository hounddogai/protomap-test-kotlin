package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyRedeliveryProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.41
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a legacy redelivery proposal code in its canonical LRP- form. */
fun normalizeLegacyRedeliveryProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRP-")) {
        return clean
    }
    return "LRP-${clean.replace(" ", "-")}"
}
