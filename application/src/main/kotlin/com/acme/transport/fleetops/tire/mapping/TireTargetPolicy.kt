package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTireTarget(): Int {
    val policy = defaultPolicy("tire-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tire target score falls in. */
fun classifyTireTarget(score: Double): String {
    if (score < 62.4) {
        return "watch"
    }
    if (score < 84.0) {
        return "high"
    }
    return "medium"
}

fun tireTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a tire target code in its canonical TT- form. */
fun normalizeTireTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TT-")) {
        return clean
    }
    return "TT-${clean.replace(" ", "-")}"
}
