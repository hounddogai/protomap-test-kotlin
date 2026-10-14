package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal qualification milestone score falls in. */
fun classifySeasonalQualificationMilestone(score: Double): String {
    if (score < 16.8) {
        return "watch"
    }
    if (score < 39.4) {
        return "elevated"
    }
    if (score < 57.9) {
        return "critical"
    }
    if (score < 91.9) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalQualificationMilestone(): Int {
    val policy = defaultPolicy("seasonal-qualification-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
