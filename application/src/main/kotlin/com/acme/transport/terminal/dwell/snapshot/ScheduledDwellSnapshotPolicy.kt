package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun scheduledDwellSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeScheduledDwellSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled dwell snapshot code in its canonical SDS- form. */
fun normalizeScheduledDwellSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDS-")) {
        return clean
    }
    return "SDS-${clean.replace(" ", "-")}"
}
