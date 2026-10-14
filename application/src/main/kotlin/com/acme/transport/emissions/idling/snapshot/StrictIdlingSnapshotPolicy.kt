package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a strict idling snapshot code in its canonical SIS- form. */
fun normalizeStrictIdlingSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIS-")) {
        return clean
    }
    return "SIS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictIdlingSnapshot(): Int {
    val policy = defaultPolicy("strict-idling-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun strictIdlingSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
