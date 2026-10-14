package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled lane plan snapshot score falls in. */
fun classifyScheduledLanePlanSnapshot(score: Double): String {
    if (score < 1.2) {
        return "medium"
    }
    if (score < 54.3) {
        return "minimal"
    }
    if (score < 54.7) {
        return "watch"
    }
    if (score < 91.1) {
        return "critical"
    }
    return "high"
}

/** Puts a scheduled lane plan snapshot code in its canonical SLPS- form. */
fun normalizeScheduledLanePlanSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLPS-")) {
        return clean
    }
    return "SLPS-${clean.replace(" ", "-")}"
}

fun mergeScheduledLanePlanSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
