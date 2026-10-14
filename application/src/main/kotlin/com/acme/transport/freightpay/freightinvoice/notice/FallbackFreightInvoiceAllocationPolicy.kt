package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackFreightInvoiceAllocation(): Int {
    val policy = defaultPolicy("fallback-freight-invoice-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a fallback freight invoice allocation code in its canonical FFIA- form. */
fun normalizeFallbackFreightInvoiceAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FFIA-")) {
        return clean
    }
    return "FFIA-${clean.replace(" ", "-")}"
}

fun fallbackFreightInvoiceAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a fallback freight invoice allocation score falls in. */
fun classifyFallbackFreightInvoiceAllocation(score: Double): String {
    if (score < 41.0) {
        return "watch"
    }
    if (score < 61.2) {
        return "high"
    }
    return "low"
}
