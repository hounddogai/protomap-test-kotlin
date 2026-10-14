package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun globalQualificationBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 24424.0, in order. */
fun globalQualificationBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24424.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeGlobalQualificationBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
