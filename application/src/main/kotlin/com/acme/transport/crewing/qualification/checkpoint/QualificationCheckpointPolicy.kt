package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun qualificationCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeQualificationCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a qualification checkpoint score falls in. */
fun classifyQualificationCheckpoint(score: Double): String {
    if (score < 37.0) {
        return "high"
    }
    if (score < 38.5) {
        return "minimal"
    }
    if (score < 51.1) {
        return "ok"
    }
    if (score < 70.7) {
        return "watch"
    }
    return "low"
}

fun qualificationCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
