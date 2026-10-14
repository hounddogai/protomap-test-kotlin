package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global cross dock schedule code in its canonical GCDS- form. */
fun normalizeGlobalCrossDockScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCDS-")) {
        return clean
    }
    return "GCDS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalCrossDockSchedule(): Int {
    val policy = defaultPolicy("global-cross-dock-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
