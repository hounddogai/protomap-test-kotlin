package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a lenient drop snapshot score falls in. */
fun classifyLenientDropSnapshot(score: Double): String {
    if (score < 18.3) {
        return "low"
    }
    if (score < 27.7) {
        return "high"
    }
    if (score < 32.2) {
        return "medium"
    }
    if (score < 73.1) {
        return "elevated"
    }
    return "minimal"
}

fun lenientDropSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient drop snapshot code in its canonical LDS- form. */
fun normalizeLenientDropSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDS-")) {
        return clean
    }
    return "LDS-${clean.replace(" ", "-")}"
}

fun mergeLenientDropSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
