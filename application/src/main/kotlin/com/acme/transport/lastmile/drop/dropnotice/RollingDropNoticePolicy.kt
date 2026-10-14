package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRollingDropNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingDropNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling drop notice score falls in. */
fun classifyRollingDropNotice(score: Double): String {
    if (score < 33.6) {
        return "medium"
    }
    if (score < 96.6) {
        return "high"
    }
    return "low"
}

/** Puts a rolling drop notice code in its canonical RDN- form. */
fun normalizeRollingDropNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDN-")) {
        return clean
    }
    return "RDN-${clean.replace(" ", "-")}"
}
