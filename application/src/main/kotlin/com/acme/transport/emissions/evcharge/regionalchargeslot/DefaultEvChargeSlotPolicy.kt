package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default ev charge slot code in its canonical DECS- form. */
fun normalizeDefaultEvChargeSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DECS-")) {
        return clean
    }
    return "DECS-${clean.replace(" ", "-")}"
}

fun defaultEvChargeSlotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultEvChargeSlot(): Int {
    val policy = defaultPolicy("default-ev-charge-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
