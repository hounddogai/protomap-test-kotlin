package com.acme.transport.customs.hold.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun holdScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeHoldScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a hold schedule score falls in. */
fun classifyHoldSchedule(score: Double): String {
    if (score < 13.3) {
        return "low"
    }
    if (score < 29.8) {
        return "minimal"
    }
    if (score < 75.9) {
        return "elevated"
    }
    if (score < 92.9) {
        return "high"
    }
    return "watch"
}
