package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.numbers.roundTo

fun batchedServiceAreaExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a batched service area exception code in its canonical BSAE- form. */
fun normalizeBatchedServiceAreaExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSAE-")) {
        return clean
    }
    return "BSAE-${clean.replace(" ", "-")}"
}
