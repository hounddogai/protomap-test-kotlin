package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnRequest(): Int {
    val policy = defaultPolicy("lease-return-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lease return request code in its canonical LRR- form. */
fun normalizeLeaseReturnRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRR-")) {
        return clean
    }
    return "LRR-${clean.replace(" ", "-")}"
}

fun leaseReturnRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
