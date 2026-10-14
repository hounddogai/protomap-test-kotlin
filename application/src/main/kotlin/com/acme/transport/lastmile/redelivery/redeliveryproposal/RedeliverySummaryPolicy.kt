package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRedeliverySummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun redeliverySummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a redelivery summary code in its canonical RS- form. */
fun normalizeRedeliverySummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}

/** Names the band a redelivery summary score falls in. */
fun classifyRedeliverySummary(score: Double): String {
    if (score < 4.2) {
        return "medium"
    }
    if (score < 23.6) {
        return "ok"
    }
    if (score < 85.1) {
        return "critical"
    }
    return "high"
}
