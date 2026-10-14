package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeBatchedCameraEventForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun batchedCameraEventForecastBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
