package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a legacy sortation digest score falls in. */
fun classifyLegacySortationDigest(score: Double): String {
    if (score < 16.4) {
        return "watch"
    }
    if (score < 95.5) {
        return "high"
    }
    return "low"
}

fun legacySortationDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 44319.0, in order. */
fun legacySortationDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44319.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLegacySortationDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
