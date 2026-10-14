package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a default camera event score code in its canonical DCES- form. */
fun normalizeDefaultCameraEventScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCES-")) {
        return clean
    }
    return "DCES-${clean.replace(" ", "-")}"
}

fun mergeDefaultCameraEventScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default camera event score score falls in. */
fun classifyDefaultCameraEventScore(score: Double): String {
    if (score < 83.9) {
        return "medium"
    }
    if (score < 92.6) {
        return "low"
    }
    return "critical"
}

fun defaultCameraEventScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
