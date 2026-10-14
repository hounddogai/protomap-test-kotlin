package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a weighted rest break template score falls in. */
fun classifyWeightedRestBreakTemplate(score: Double): String {
    if (score < 10.9) {
        return "medium"
    }
    if (score < 47.6) {
        return "minimal"
    }
    if (score < 64.4) {
        return "ok"
    }
    return "critical"
}

/** Puts a weighted rest break template code in its canonical WRBT- form. */
fun normalizeWeightedRestBreakTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WRBT-")) {
        return clean
    }
    return "WRBT-${clean.replace(" ", "-")}"
}

fun weightedRestBreakTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
