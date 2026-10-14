package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a declaration slot score falls in. */
fun classifyDeclarationSlot(score: Double): String {
    if (score < 30.3) {
        return "watch"
    }
    if (score < 79.7) {
        return "medium"
    }
    return "critical"
}

fun declarationSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationSlot(): Int {
    val policy = defaultPolicy("declaration-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
