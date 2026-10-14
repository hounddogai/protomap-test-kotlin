package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tire milestone score falls in. */
fun classifyTireMilestone(score: Double): String {
    if (score < 12.1) {
        return "critical"
    }
    if (score < 26.1) {
        return "low"
    }
    if (score < 36.0) {
        return "minimal"
    }
    if (score < 66.0) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTireMilestone(): Int {
    val policy = defaultPolicy("tire-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tire milestone code in its canonical TM- form. */
fun normalizeTireMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}
