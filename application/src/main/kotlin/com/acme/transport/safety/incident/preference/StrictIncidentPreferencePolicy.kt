package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a strict incident preference code in its canonical SIP- form. */
fun normalizeStrictIncidentPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIP-")) {
        return clean
    }
    return "SIP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 25591.0, in order. */
fun strictIncidentPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25591.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeStrictIncidentPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
