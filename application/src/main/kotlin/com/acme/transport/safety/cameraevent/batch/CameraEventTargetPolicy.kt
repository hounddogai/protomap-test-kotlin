package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 31697.0, in order. */
fun cameraEventTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31697.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCameraEventTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event target score falls in. */
fun classifyCameraEventTarget(score: Double): String {
    if (score < 2.0) {
        return "high"
    }
    if (score < 27.6) {
        return "watch"
    }
    if (score < 57.4) {
        return "minimal"
    }
    if (score < 71.8) {
        return "low"
    }
    return "ok"
}

fun cameraEventTargetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
