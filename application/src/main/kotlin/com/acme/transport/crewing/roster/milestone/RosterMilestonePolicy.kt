package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a roster milestone score falls in. */
fun classifyRosterMilestone(score: Double): String {
    if (score < 4.1) {
        return "high"
    }
    if (score < 6.7) {
        return "elevated"
    }
    if (score < 65.2) {
        return "critical"
    }
    return "ok"
}

fun mergeRosterMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a roster milestone code in its canonical RM- form. */
fun normalizeRosterMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RM-")) {
        return clean
    }
    return "RM-${clean.replace(" ", "-")}"
}

fun rosterMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
