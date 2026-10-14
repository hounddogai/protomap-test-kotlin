package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a lenient dwell schedule code in its canonical LDS- form. */
fun normalizeLenientDwellScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDS-")) {
        return clean
    }
    return "LDS-${clean.replace(" ", "-")}"
}

fun lenientDwellScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientDwellSchedule(): Int {
    val policy = defaultPolicy("lenient-dwell-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
