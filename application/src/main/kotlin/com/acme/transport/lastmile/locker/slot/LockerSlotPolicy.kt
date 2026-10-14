package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerSlot(): Int {
    val policy = defaultPolicy("locker-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a locker slot code in its canonical LS- form. */
fun normalizeLockerSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LS-")) {
        return clean
    }
    return "LS-${clean.replace(" ", "-")}"
}
