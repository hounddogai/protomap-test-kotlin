package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.numbers.roundTo

fun lenientCapacityMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun lenientCapacityMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a lenient capacity milestone code in its canonical LCM- form. */
fun normalizeLenientCapacityMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCM-")) {
        return clean
    }
    return "LCM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 65255.0, in order. */
fun lenientCapacityMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65255.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
