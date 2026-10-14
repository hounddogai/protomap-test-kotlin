package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched idling variance code in its canonical BIV- form. */
fun normalizeBatchedIdlingVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIV-")) {
        return clean
    }
    return "BIV-${clean.replace(" ", "-")}"
}

fun batchedIdlingVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedIdlingVariance(): Int {
    val policy = defaultPolicy("batched-idling-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
