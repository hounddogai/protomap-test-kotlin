package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a default backhaul window code in its canonical DBW- form. */
fun normalizeDefaultBackhaulWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DBW-")) {
        return clean
    }
    return "DBW-${clean.replace(" ", "-")}"
}

/** Names the band a default backhaul window score falls in. */
fun classifyDefaultBackhaulWindow(score: Double): String {
    if (score < 5.1) {
        return "high"
    }
    if (score < 17.0) {
        return "critical"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 70935.0, in order. */
fun defaultBackhaulWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70935.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDefaultBackhaulWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
