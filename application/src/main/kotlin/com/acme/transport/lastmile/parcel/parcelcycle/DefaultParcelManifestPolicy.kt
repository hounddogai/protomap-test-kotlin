package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a default parcel manifest score falls in. */
fun classifyDefaultParcelManifest(score: Double): String {
    if (score < 19.2) {
        return "watch"
    }
    if (score < 33.3) {
        return "ok"
    }
    if (score < 35.4) {
        return "elevated"
    }
    if (score < 81.5) {
        return "critical"
    }
    return "medium"
}

fun mergeDefaultParcelManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 99442.0, in order. */
fun defaultParcelManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99442.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultParcelManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
