package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a inspection checkpoint code in its canonical IC- form. */
fun normalizeInspectionCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IC-")) {
        return clean
    }
    return "IC-${clean.replace(" ", "-")}"
}

/** Names the band a inspection checkpoint score falls in. */
fun classifyInspectionCheckpoint(score: Double): String {
    if (score < 1.5) {
        return "low"
    }
    if (score < 44.3) {
        return "medium"
    }
    if (score < 45.3) {
        return "watch"
    }
    if (score < 95.3) {
        return "high"
    }
    return "ok"
}

fun inspectionCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeInspectionCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
