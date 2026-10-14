package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a scheduled dock door claim score falls in. */
fun classifyScheduledDockDoorClaim(score: Double): String {
    if (score < 13.1) {
        return "low"
    }
    if (score < 33.0) {
        return "minimal"
    }
    if (score < 91.4) {
        return "critical"
    }
    if (score < 92.7) {
        return "watch"
    }
    return "medium"
}

fun scheduledDockDoorClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeScheduledDockDoorClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled dock door claim code in its canonical SDDC- form. */
fun normalizeScheduledDockDoorClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDC-")) {
        return clean
    }
    return "SDDC-${clean.replace(" ", "-")}"
}
