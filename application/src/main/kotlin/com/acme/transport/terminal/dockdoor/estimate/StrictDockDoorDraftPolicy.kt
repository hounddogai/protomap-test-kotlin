package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a strict dock door draft score falls in. */
fun classifyStrictDockDoorDraft(score: Double): String {
    if (score < 44.9) {
        return "critical"
    }
    if (score < 62.6) {
        return "low"
    }
    if (score < 89.9) {
        return "elevated"
    }
    if (score < 93.3) {
        return "watch"
    }
    return "high"
}

fun mergeStrictDockDoorDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun strictDockDoorDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
