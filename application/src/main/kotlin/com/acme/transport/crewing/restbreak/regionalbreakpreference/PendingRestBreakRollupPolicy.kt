package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePendingRestBreakRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending rest break rollup code in its canonical PRBR- form. */
fun normalizePendingRestBreakRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRBR-")) {
        return clean
    }
    return "PRBR-${clean.replace(" ", "-")}"
}

fun pendingRestBreakRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 2094.0, in order. */
fun pendingRestBreakRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2094.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
