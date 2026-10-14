package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun capacityProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a capacity profile score falls in. */
fun classifyCapacityProfile(score: Double): String {
    if (score < 29.7) {
        return "high"
    }
    if (score < 83.3) {
        return "critical"
    }
    return "low"
}

fun mergeCapacityProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a capacity profile code in its canonical CP- form. */
fun normalizeCapacityProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CP-")) {
        return clean
    }
    return "CP-${clean.replace(" ", "-")}"
}
