package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalCoachingSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 61137.0, in order. */
fun globalCoachingSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61137.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global coaching segment code in its canonical GCS- form. */
fun normalizeGlobalCoachingSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCS-")) {
        return clean
    }
    return "GCS-${clean.replace(" ", "-")}"
}
