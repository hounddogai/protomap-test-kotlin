package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a dock door draft score falls in. */
fun classifyDockDoorDraft(score: Double): String {
    if (score < 54.6) {
        return "elevated"
    }
    if (score < 83.7) {
        return "watch"
    }
    return "medium"
}

fun mergeDockDoorDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
