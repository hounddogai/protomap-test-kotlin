package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a regional proof of delivery score code in its canonical RPODS- form. */
fun normalizeRegionalProofOfDeliveryScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODS-")) {
        return clean
    }
    return "RPODS-${clean.replace(" ", "-")}"
}

fun mergeRegionalProofOfDeliveryScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalProofOfDeliveryScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
