package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a availability schedule score falls in. */
fun classifyAvailabilitySchedule(score: Double): String {
    if (score < 3.0) {
        return "high"
    }
    if (score < 24.0) {
        return "watch"
    }
    if (score < 67.5) {
        return "medium"
    }
    return "minimal"
}

/** Puts a availability schedule code in its canonical AS- form. */
fun normalizeAvailabilityScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

fun mergeAvailabilityScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
