package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockSegment(): Int {
    val policy = defaultPolicy("cross-dock-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a cross dock segment code in its canonical CDS- form. */
fun normalizeCrossDockSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDS-")) {
        return clean
    }
    return "CDS-${clean.replace(" ", "-")}"
}

fun crossDockSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
