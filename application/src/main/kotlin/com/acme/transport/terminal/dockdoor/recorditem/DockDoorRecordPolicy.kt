package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun dockDoorRecordBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a dock door record score falls in. */
fun classifyDockDoorRecord(score: Double): String {
    if (score < 11.5) {
        return "ok"
    }
    if (score < 44.8) {
        return "high"
    }
    if (score < 86.2) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorRecord(): Int {
    val policy = defaultPolicy("dock-door-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
