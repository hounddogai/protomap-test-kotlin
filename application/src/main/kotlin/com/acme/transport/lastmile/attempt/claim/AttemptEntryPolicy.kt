package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeAttemptEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun attemptEntryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 41001.0, in order. */
fun attemptEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41001.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a attempt entry code in its canonical AE- form. */
fun normalizeAttemptEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AE-")) {
        return clean
    }
    return "AE-${clean.replace(" ", "-")}"
}
