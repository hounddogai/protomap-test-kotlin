package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeScheduledServicePlanBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled service plan budget score falls in. */
fun classifyScheduledServicePlanBudget(score: Double): String {
    if (score < 36.8) {
        return "critical"
    }
    if (score < 38.8) {
        return "watch"
    }
    if (score < 77.2) {
        return "minimal"
    }
    if (score < 97.7) {
        return "elevated"
    }
    return "low"
}

/** Puts a scheduled service plan budget code in its canonical SSPB- form. */
fun normalizeScheduledServicePlanBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSPB-")) {
        return clean
    }
    return "SSPB-${clean.replace(" ", "-")}"
}

fun scheduledServicePlanBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
