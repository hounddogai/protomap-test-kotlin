package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global dock door claim code in its canonical GDDC- form. */
fun normalizeGlobalDockDoorClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDDC-")) {
        return clean
    }
    return "GDDC-${clean.replace(" ", "-")}"
}

fun globalDockDoorClaimSpan(start: Int, end: Int): Int {
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
fun probeGlobalDockDoorClaim(): Int {
    val policy = defaultPolicy("global-dock-door-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
