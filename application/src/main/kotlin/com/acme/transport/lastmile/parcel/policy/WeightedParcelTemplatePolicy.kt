package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a weighted parcel template score falls in. */
fun classifyWeightedParcelTemplate(score: Double): String {
    if (score < 21.9) {
        return "ok"
    }
    if (score < 63.4) {
        return "medium"
    }
    if (score < 80.6) {
        return "watch"
    }
    return "elevated"
}

fun weightedParcelTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a weighted parcel template code in its canonical WPT- form. */
fun normalizeWeightedParcelTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WPT-")) {
        return clean
    }
    return "WPT-${clean.replace(" ", "-")}"
}

fun weightedParcelTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
