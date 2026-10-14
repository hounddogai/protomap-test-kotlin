package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 74244.0, in order. */
fun qualificationSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74244.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeQualificationSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a qualification snapshot code in its canonical QS- form. */
fun normalizeQualificationSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QS-")) {
        return clean
    }
    return "QS-${clean.replace(" ", "-")}"
}

/** Names the band a qualification snapshot score falls in. */
fun classifyQualificationSnapshot(score: Double): String {
    if (score < 16.2) {
        return "medium"
    }
    if (score < 51.7) {
        return "critical"
    }
    if (score < 82.8) {
        return "watch"
    }
    if (score < 89.1) {
        return "ok"
    }
    return "minimal"
}
