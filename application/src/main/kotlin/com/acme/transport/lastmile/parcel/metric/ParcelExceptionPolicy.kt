package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a parcel exception score falls in. */
fun classifyParcelException(score: Double): String {
    if (score < 49.6) {
        return "watch"
    }
    if (score < 71.8) {
        return "minimal"
    }
    if (score < 84.3) {
        return "critical"
    }
    if (score < 89.2) {
        return "elevated"
    }
    return "low"
}

fun parcelExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelException(): Int {
    val policy = defaultPolicy("parcel-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a parcel exception code in its canonical PE- form. */
fun normalizeParcelExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PE-")) {
        return clean
    }
    return "PE-${clean.replace(" ", "-")}"
}
