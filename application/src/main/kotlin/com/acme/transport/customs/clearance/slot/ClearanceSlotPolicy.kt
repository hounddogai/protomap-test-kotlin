package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a clearance slot code in its canonical CS- form. */
fun normalizeClearanceSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}

/** Names the band a clearance slot score falls in. */
fun classifyClearanceSlot(score: Double): String {
    if (score < 34.6) {
        return "minimal"
    }
    if (score < 67.1) {
        return "critical"
    }
    if (score < 68.9) {
        return "medium"
    }
    return "low"
}

fun clearanceSlotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceSlot(): Int {
    val policy = defaultPolicy("clearance-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
