package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a idling exception code in its canonical IE- form. */
fun normalizeIdlingExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IE-")) {
        return clean
    }
    return "IE-${clean.replace(" ", "-")}"
}

/** Names the band a idling exception score falls in. */
fun classifyIdlingException(score: Double): String {
    if (score < 61.2) {
        return "ok"
    }
    if (score < 98.2) {
        return "critical"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 57475.0, in order. */
fun idlingExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57475.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeIdlingExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
