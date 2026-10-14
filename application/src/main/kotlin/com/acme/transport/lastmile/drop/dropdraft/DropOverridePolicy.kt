package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a drop override score falls in. */
fun classifyDropOverride(score: Double): String {
    if (score < 9.1) {
        return "minimal"
    }
    if (score < 54.9) {
        return "watch"
    }
    return "medium"
}

/** Puts a drop override code in its canonical DO- form. */
fun normalizeDropOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DO-")) {
        return clean
    }
    return "DO-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropOverride(): Int {
    val policy = defaultPolicy("drop-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
