package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedAccessorialEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a capped accessorial entry code in its canonical CAE- form. */
fun normalizeCappedAccessorialEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAE-")) {
        return clean
    }
    return "CAE-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedAccessorialEntry(): Int {
    val policy = defaultPolicy("capped-accessorial-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
