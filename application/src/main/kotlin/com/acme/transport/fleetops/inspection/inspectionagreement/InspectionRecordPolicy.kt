package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeInspectionRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection record score falls in. */
fun classifyInspectionRecord(score: Double): String {
    if (score < 55.4) {
        return "critical"
    }
    if (score < 76.9) {
        return "low"
    }
    if (score < 97.6) {
        return "watch"
    }
    return "elevated"
}

/** Puts a inspection record code in its canonical IR- form. */
fun normalizeInspectionRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IR-")) {
        return clean
    }
    return "IR-${clean.replace(" ", "-")}"
}
