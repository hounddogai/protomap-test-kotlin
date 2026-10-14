package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a inspection mapping code in its canonical IM- form. */
fun normalizeInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IM-")) {
        return clean
    }
    return "IM-${clean.replace(" ", "-")}"
}

fun inspectionMappingBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionMapping(): Int {
    val policy = defaultPolicy("inspection-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
