package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDockDoorAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a dock door assignment code in its canonical DDA- form. */
fun normalizeDockDoorAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDA-")) {
        return clean
    }
    return "DDA-${clean.replace(" ", "-")}"
}

fun dockDoorAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 49003.0, in order. */
fun dockDoorAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49003.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
