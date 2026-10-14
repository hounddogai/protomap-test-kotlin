package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a rolling proof of delivery preference code in its canonical RPODP- form. */
fun normalizeRollingProofOfDeliveryPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODP-")) {
        return clean
    }
    return "RPODP-${clean.replace(" ", "-")}"
}

/** Names the band a rolling proof of delivery preference score falls in. */
fun classifyRollingProofOfDeliveryPreference(score: Double): String {
    if (score < 6.9) {
        return "medium"
    }
    if (score < 8.7) {
        return "minimal"
    }
    if (score < 23.4) {
        return "ok"
    }
    if (score < 35.9) {
        return "watch"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 54331.0, in order. */
fun rollingProofOfDeliveryPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54331.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRollingProofOfDeliveryPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
