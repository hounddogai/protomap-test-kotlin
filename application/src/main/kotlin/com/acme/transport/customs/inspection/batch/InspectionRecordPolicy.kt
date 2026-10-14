package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 43855.0, in order. */
fun inspectionRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43855.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a inspection record code in its canonical IR- form. */
fun normalizeInspectionRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IR-")) {
        return clean
    }
    return "IR-${clean.replace(" ", "-")}"
}

fun mergeInspectionRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection record score falls in. */
fun classifyInspectionRecord(score: Double): String {
    if (score < 14.7) {
        return "critical"
    }
    if (score < 94.6) {
        return "low"
    }
    return "medium"
}
