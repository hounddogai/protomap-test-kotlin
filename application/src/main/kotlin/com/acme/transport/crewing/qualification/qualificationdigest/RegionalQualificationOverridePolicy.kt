package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalQualificationOverride(): Int {
    val policy = defaultPolicy("regional-qualification-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalQualificationOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional qualification override score falls in. */
fun classifyRegionalQualificationOverride(score: Double): String {
    if (score < 87.7) {
        return "critical"
    }
    if (score < 93.9) {
        return "medium"
    }
    return "elevated"
}
