package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackRestBreakCheckpoint(): Int {
    val policy = defaultPolicy("fallback-rest-break-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback rest break checkpoint score falls in. */
fun classifyFallbackRestBreakCheckpoint(score: Double): String {
    if (score < 52.7) {
        return "watch"
    }
    if (score < 96.6) {
        return "medium"
    }
    return "low"
}

/** Puts a fallback rest break checkpoint code in its canonical FRBC- form. */
fun normalizeFallbackRestBreakCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FRBC-")) {
        return clean
    }
    return "FRBC-${clean.replace(" ", "-")}"
}
