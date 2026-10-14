package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a ev charge allocation score falls in. */
fun classifyEvChargeAllocation(score: Double): String {
    if (score < 5.5) {
        return "watch"
    }
    if (score < 69.4) {
        return "low"
    }
    return "high"
}

fun evChargeAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a ev charge allocation code in its canonical ECA- form. */
fun normalizeEvChargeAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECA-")) {
        return clean
    }
    return "ECA-${clean.replace(" ", "-")}"
}

fun mergeEvChargeAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
