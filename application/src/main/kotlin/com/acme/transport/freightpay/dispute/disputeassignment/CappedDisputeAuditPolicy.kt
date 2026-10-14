package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a capped dispute audit code in its canonical CDA- form. */
fun normalizeCappedDisputeAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDA-")) {
        return clean
    }
    return "CDA-${clean.replace(" ", "-")}"
}

fun cappedDisputeAuditBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDisputeAudit(): Int {
    val policy = defaultPolicy("capped-dispute-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
