package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorNotice(): Int {
    val policy = defaultPolicy("dock-door-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dockDoorNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a dock door notice score falls in. */
fun classifyDockDoorNotice(score: Double): String {
    if (score < 15.5) {
        return "watch"
    }
    if (score < 40.3) {
        return "medium"
    }
    if (score < 60.2) {
        return "minimal"
    }
    if (score < 67.4) {
        return "elevated"
    }
    return "ok"
}
