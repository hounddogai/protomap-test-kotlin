package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDockDoorTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a dock door template score falls in. */
fun classifyDockDoorTemplate(score: Double): String {
    if (score < 1.3) {
        return "minimal"
    }
    if (score < 72.9) {
        return "low"
    }
    if (score < 88.9) {
        return "critical"
    }
    if (score < 90.6) {
        return "watch"
    }
    return "elevated"
}
