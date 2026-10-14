package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a adaptive backhaul milestone score falls in. */
fun classifyAdaptiveBackhaulMilestone(score: Double): String {
    if (score < 30.6) {
        return "critical"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "watch"
}

fun adaptiveBackhaulMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveBackhaulMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a adaptive backhaul milestone code in its canonical ABM- form. */
fun normalizeAdaptiveBackhaulMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ABM-")) {
        return clean
    }
    return "ABM-${clean.replace(" ", "-")}"
}
