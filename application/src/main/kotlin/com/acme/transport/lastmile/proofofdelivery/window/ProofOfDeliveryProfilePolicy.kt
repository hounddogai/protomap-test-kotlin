package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a proof of delivery profile code in its canonical PODP- form. */
fun normalizeProofOfDeliveryProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODP-")) {
        return clean
    }
    return "PODP-${clean.replace(" ", "-")}"
}

fun mergeProofOfDeliveryProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 4302.0, in order. */
fun proofOfDeliveryProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4302.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun proofOfDeliveryProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
