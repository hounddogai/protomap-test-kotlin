package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a manual camera event digest score falls in. */
fun classifyManualCameraEventDigest(score: Double): String {
    if (score < 26.7) {
        return "ok"
    }
    if (score < 64.3) {
        return "elevated"
    }
    if (score < 75.4) {
        return "minimal"
    }
    if (score < 83.5) {
        return "watch"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 63269.0, in order. */
fun manualCameraEventDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63269.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeManualCameraEventDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualCameraEventDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
