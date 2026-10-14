package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 83313.0, in order. */
fun strictServiceAreaBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83313.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictServiceAreaBundleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeStrictServiceAreaBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a strict service area bundle code in its canonical SSAB- form. */
fun normalizeStrictServiceAreaBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSAB-")) {
        return clean
    }
    return "SSAB-${clean.replace(" ", "-")}"
}
