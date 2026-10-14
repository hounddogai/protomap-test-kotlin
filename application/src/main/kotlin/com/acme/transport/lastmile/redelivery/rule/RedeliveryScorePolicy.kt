package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRedeliveryScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a redelivery score score falls in. */
fun classifyRedeliveryScore(score: Double): String {
    if (score < 68.7) {
        return "minimal"
    }
    if (score < 78.2) {
        return "ok"
    }
    return "low"
}

/** Puts a redelivery score code in its canonical RS- form. */
fun normalizeRedeliveryScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}

fun redeliveryScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
