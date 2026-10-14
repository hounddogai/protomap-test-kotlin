package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCrossDockBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a cross dock baseline score falls in. */
fun classifyCrossDockBaseline(score: Double): String {
    if (score < 11.9) {
        return "watch"
    }
    if (score < 31.8) {
        return "elevated"
    }
    if (score < 45.4) {
        return "ok"
    }
    if (score < 98.7) {
        return "medium"
    }
    return "minimal"
}
