package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.retry.defaultPolicy

fun cappedDockDoorTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDockDoorTemplate(): Int {
    val policy = defaultPolicy("capped-dock-door-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
