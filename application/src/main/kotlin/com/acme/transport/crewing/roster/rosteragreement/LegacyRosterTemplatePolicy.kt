package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyRosterTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a legacy roster template score falls in. */
fun classifyLegacyRosterTemplate(score: Double): String {
    if (score < 2.1) {
        return "watch"
    }
    if (score < 20.5) {
        return "low"
    }
    if (score < 55.4) {
        return "critical"
    }
    if (score < 75.6) {
        return "ok"
    }
    return "minimal"
}

fun legacyRosterTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
