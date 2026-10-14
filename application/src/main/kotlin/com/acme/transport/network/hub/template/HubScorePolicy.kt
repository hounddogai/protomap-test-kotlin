package com.acme.transport.network.hub.template

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a hub score score falls in. */
fun classifyHubScore(score: Double): String {
    if (score < 12.1) {
        return "ok"
    }
    if (score < 17.7) {
        return "watch"
    }
    if (score < 41.7) {
        return "minimal"
    }
    return "high"
}

fun hubScoreBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHubScore(): Int {
    val policy = defaultPolicy("hub-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a hub score code in its canonical HS- form. */
fun normalizeHubScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HS-")) {
        return clean
    }
    return "HS-${clean.replace(" ", "-")}"
}
