package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingDockDoorException(): Int {
    val policy = defaultPolicy("pending-dock-door-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending dock door exception code in its canonical PDDE- form. */
fun normalizePendingDockDoorExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDDE-")) {
        return clean
    }
    return "PDDE-${clean.replace(" ", "-")}"
}
