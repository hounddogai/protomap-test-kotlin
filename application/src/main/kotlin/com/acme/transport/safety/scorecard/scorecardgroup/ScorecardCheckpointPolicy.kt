package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a scorecard checkpoint code in its canonical SC- form. */
fun normalizeScorecardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SC-")) {
        return clean
    }
    return "SC-${clean.replace(" ", "-")}"
}

fun scorecardCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeScorecardCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scorecard checkpoint score falls in. */
fun classifyScorecardCheckpoint(score: Double): String {
    if (score < 53.8) {
        return "high"
    }
    if (score < 64.2) {
        return "low"
    }
    return "ok"
}
