package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDockDoorBaseline(): Int {
    val policy = defaultPolicy("capped-dock-door-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped dock door baseline code in its canonical CDDB- form. */
fun normalizeCappedDockDoorBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDDB-")) {
        return clean
    }
    return "CDDB-${clean.replace(" ", "-")}"
}
