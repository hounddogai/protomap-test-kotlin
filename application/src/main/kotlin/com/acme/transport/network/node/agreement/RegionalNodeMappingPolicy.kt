package com.acme.transport.network.node.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalNodeMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a regional node mapping code in its canonical RNM- form. */
fun normalizeRegionalNodeMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNM-")) {
        return clean
    }
    return "RNM-${clean.replace(" ", "-")}"
}

/** Names the band a regional node mapping score falls in. */
fun classifyRegionalNodeMapping(score: Double): String {
    if (score < 64.5) {
        return "ok"
    }
    if (score < 80.1) {
        return "medium"
    }
    return "low"
}
