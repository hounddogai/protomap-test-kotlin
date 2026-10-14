package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lenient service area window score falls in. */
fun classifyLenientServiceAreaWindow(score: Double): String {
    if (score < 4.7) {
        return "ok"
    }
    if (score < 61.9) {
        return "high"
    }
    if (score < 70.7) {
        return "critical"
    }
    if (score < 70.8) {
        return "elevated"
    }
    return "watch"
}

fun mergeLenientServiceAreaWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient service area window code in its canonical LSAW- form. */
fun normalizeLenientServiceAreaWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSAW-")) {
        return clean
    }
    return "LSAW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 68972.0, in order. */
fun lenientServiceAreaWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68972.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
