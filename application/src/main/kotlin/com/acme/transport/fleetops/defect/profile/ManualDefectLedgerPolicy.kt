package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDefectLedger(): Int {
    val policy = defaultPolicy("manual-defect-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualDefectLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual defect ledger code in its canonical MDL- form. */
fun normalizeManualDefectLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDL-")) {
        return clean
    }
    return "MDL-${clean.replace(" ", "-")}"
}
