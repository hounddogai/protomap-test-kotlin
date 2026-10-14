package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun primaryInspectionMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryInspectionMapping(): Int {
    val policy = defaultPolicy("primary-inspection-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary inspection mapping code in its canonical PIM- form. */
fun normalizePrimaryInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIM-")) {
        return clean
    }
    return "PIM-${clean.replace(" ", "-")}"
}
