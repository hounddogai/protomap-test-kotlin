package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun legacyCameraEventAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyCameraEventAdjustment(): Int {
    val policy = defaultPolicy("legacy-camera-event-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy camera event adjustment code in its canonical LCEA- form. */
fun normalizeLegacyCameraEventAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCEA-")) {
        return clean
    }
    return "LCEA-${clean.replace(" ", "-")}"
}
