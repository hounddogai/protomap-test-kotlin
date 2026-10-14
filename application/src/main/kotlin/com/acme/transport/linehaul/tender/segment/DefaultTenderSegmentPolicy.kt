package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTenderSegment(): Int {
    val policy = defaultPolicy("default-tender-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a default tender segment code in its canonical DTS- form. */
fun normalizeDefaultTenderSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTS-")) {
        return clean
    }
    return "DTS-${clean.replace(" ", "-")}"
}
