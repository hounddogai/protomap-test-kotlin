package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched dock door window code in its canonical BDDW- form. */
fun normalizeBatchedDockDoorWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDDW-")) {
        return clean
    }
    return "BDDW-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDockDoorWindow(): Int {
    val policy = defaultPolicy("batched-dock-door-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
