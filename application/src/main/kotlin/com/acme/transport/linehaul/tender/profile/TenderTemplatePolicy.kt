package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tenderTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun tenderTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tender template score falls in. */
fun classifyTenderTemplate(score: Double): String {
    if (score < 14.7) {
        return "ok"
    }
    if (score < 48.3) {
        return "medium"
    }
    return "minimal"
}
