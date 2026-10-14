package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingQualificationAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending qualification assignment code in its canonical PQA- form. */
fun normalizePendingQualificationAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PQA-")) {
        return clean
    }
    return "PQA-${clean.replace(" ", "-")}"
}

/** Names the band a pending qualification assignment score falls in. */
fun classifyPendingQualificationAssignment(score: Double): String {
    if (score < 3.1) {
        return "low"
    }
    if (score < 5.5) {
        return "minimal"
    }
    if (score < 21.8) {
        return "watch"
    }
    if (score < 43.2) {
        return "ok"
    }
    return "high"
}
