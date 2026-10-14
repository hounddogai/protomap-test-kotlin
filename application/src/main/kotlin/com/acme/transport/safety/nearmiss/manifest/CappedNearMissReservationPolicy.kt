package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeCappedNearMissReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedNearMissReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedNearMissReservationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a capped near miss reservation score falls in. */
fun classifyCappedNearMissReservation(score: Double): String {
    if (score < 15.5) {
        return "medium"
    }
    if (score < 53.6) {
        return "elevated"
    }
    if (score < 97.9) {
        return "watch"
    }
    return "critical"
}
