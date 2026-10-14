package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a dock door forecast code in its canonical DDF- form. */
fun normalizeDockDoorForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDF-")) {
        return clean
    }
    return "DDF-${clean.replace(" ", "-")}"
}

fun mergeDockDoorForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dockDoorForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
