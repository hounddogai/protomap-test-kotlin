package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacyCrossDockEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun legacyCrossDockEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a legacy cross dock entry score falls in. */
fun classifyLegacyCrossDockEntry(score: Double): String {
    if (score < 67.1) {
        return "ok"
    }
    if (score < 70.7) {
        return "low"
    }
    return "high"
}
