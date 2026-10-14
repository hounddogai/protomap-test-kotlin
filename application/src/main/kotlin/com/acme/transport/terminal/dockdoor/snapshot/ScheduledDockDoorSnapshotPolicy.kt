package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a scheduled dock door snapshot code in its canonical SDDS- form. */
fun normalizeScheduledDockDoorSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDS-")) {
        return clean
    }
    return "SDDS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDockDoorSnapshot(): Int {
    val policy = defaultPolicy("scheduled-dock-door-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeScheduledDockDoorSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled dock door snapshot score falls in. */
fun classifyScheduledDockDoorSnapshot(score: Double): String {
    if (score < 9.0) {
        return "critical"
    }
    if (score < 61.9) {
        return "watch"
    }
    if (score < 78.2) {
        return "low"
    }
    return "medium"
}
