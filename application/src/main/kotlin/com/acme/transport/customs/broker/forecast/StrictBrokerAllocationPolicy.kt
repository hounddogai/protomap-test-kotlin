package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictBrokerAllocation(): Int {
    val policy = defaultPolicy("strict-broker-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict broker allocation code in its canonical SBA- form. */
fun normalizeStrictBrokerAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBA-")) {
        return clean
    }
    return "SBA-${clean.replace(" ", "-")}"
}
