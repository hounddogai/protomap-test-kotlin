package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient availability allocation score falls in. */
fun classifyLenientAvailabilityAllocation(score: Double): String {
    if (score < 57.9) {
        return "medium"
    }
    if (score < 81.0) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAvailabilityAllocation(): Int {
    val policy = defaultPolicy("lenient-availability-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientAvailabilityAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a lenient availability allocation code in its canonical LAA- form. */
fun normalizeLenientAvailabilityAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAA-")) {
        return clean
    }
    return "LAA-${clean.replace(" ", "-")}"
}
