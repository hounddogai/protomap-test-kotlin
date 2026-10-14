package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun regionalRelayThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a regional relay threshold score falls in. */
fun classifyRegionalRelayThreshold(score: Double): String {
    if (score < 6.6) {
        return "high"
    }
    if (score < 16.5) {
        return "minimal"
    }
    if (score < 80.5) {
        return "ok"
    }
    return "elevated"
}

/** Puts a regional relay threshold code in its canonical RRT- form. */
fun normalizeRegionalRelayThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRT-")) {
        return clean
    }
    return "RRT-${clean.replace(" ", "-")}"
}

fun mergeRegionalRelayThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
