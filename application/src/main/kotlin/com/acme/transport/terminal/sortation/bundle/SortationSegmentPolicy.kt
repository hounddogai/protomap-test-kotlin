package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun sortationSegmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationSegment(): Int {
    val policy = defaultPolicy("sortation-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a sortation segment code in its canonical SS- form. */
fun normalizeSortationSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}
