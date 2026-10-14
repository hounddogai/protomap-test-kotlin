package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun fallbackDockDoorGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeFallbackDockDoorGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fallback dock door group code in its canonical FDDG- form. */
fun normalizeFallbackDockDoorGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDDG-")) {
        return clean
    }
    return "FDDG-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 30086.0, in order. */
fun fallbackDockDoorGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30086.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
