package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a weighted fuel surcharge proposal code in its canonical WFSP- form. */
fun normalizeWeightedFuelSurchargeProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFSP-")) {
        return clean
    }
    return "WFSP-${clean.replace(" ", "-")}"
}

fun weightedFuelSurchargeProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedFuelSurchargeProposalBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
