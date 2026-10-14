package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling node draft code in its canonical RND- form. */
fun normalizeRollingNodeDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RND-")) {
        return clean
    }
    return "RND-${clean.replace(" ", "-")}"
}

/** Names the band a rolling node draft score falls in. */
fun classifyRollingNodeDraft(score: Double): String {
    if (score < 3.1) {
        return "elevated"
    }
    if (score < 84.9) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingNodeDraft(): Int {
    val policy = defaultPolicy("rolling-node-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
