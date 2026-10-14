package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 35398.0, in order. */
fun lockerAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35398.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLockerAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a locker adjustment code in its canonical LA- form. */
fun normalizeLockerAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LA-")) {
        return clean
    }
    return "LA-${clean.replace(" ", "-")}"
}
