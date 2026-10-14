package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeIncidentNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a incident notice code in its canonical IN- form. */
fun normalizeIncidentNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IN-")) {
        return clean
    }
    return "IN-${clean.replace(" ", "-")}"
}

/** Names the band a incident notice score falls in. */
fun classifyIncidentNotice(score: Double): String {
    if (score < 11.8) {
        return "watch"
    }
    if (score < 31.0) {
        return "high"
    }
    if (score < 58.2) {
        return "elevated"
    }
    if (score < 62.5) {
        return "critical"
    }
    return "ok"
}
