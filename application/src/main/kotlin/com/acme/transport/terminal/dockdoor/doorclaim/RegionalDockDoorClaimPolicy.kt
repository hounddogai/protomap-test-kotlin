package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a regional dock door claim score falls in. */
fun classifyRegionalDockDoorClaim(score: Double): String {
    if (score < 21.1) {
        return "minimal"
    }
    if (score < 24.7) {
        return "low"
    }
    if (score < 27.2) {
        return "critical"
    }
    if (score < 81.6) {
        return "medium"
    }
    return "ok"
}

fun mergeRegionalDockDoorClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
