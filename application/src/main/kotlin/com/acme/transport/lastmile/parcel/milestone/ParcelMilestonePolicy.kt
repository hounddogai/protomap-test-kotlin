package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a parcel milestone code in its canonical PM- form. */
fun normalizeParcelMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PM-")) {
        return clean
    }
    return "PM-${clean.replace(" ", "-")}"
}

fun parcelMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelMilestone(): Int {
    val policy = defaultPolicy("parcel-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a parcel milestone score falls in. */
fun classifyParcelMilestone(score: Double): String {
    if (score < 30.7) {
        return "low"
    }
    if (score < 74.3) {
        return "critical"
    }
    if (score < 80.9) {
        return "elevated"
    }
    if (score < 88.0) {
        return "high"
    }
    return "medium"
}
