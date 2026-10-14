package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun regionalDockDoorProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeRegionalDockDoorProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 27249.0, in order. */
fun regionalDockDoorProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27249.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalDockDoorProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
