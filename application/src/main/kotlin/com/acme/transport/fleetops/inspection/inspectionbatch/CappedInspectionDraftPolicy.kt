package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedInspectionDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a capped inspection draft code in its canonical CID- form. */
fun normalizeCappedInspectionDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CID-")) {
        return clean
    }
    return "CID-${clean.replace(" ", "-")}"
}

fun cappedInspectionDraftBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedInspectionDraft(): Int {
    val policy = defaultPolicy("capped-inspection-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
