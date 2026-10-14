package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a primary sortation milestone score falls in. */
fun classifyPrimarySortationMilestone(score: Double): String {
    if (score < 15.4) {
        return "ok"
    }
    if (score < 19.2) {
        return "low"
    }
    return "high"
}

fun primarySortationMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergePrimarySortationMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary sortation milestone code in its canonical PSM- form. */
fun normalizePrimarySortationMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSM-")) {
        return clean
    }
    return "PSM-${clean.replace(" ", "-")}"
}
