package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global freight invoice digest code in its canonical GFID- form. */
fun normalizeGlobalFreightInvoiceDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GFID-")) {
        return clean
    }
    return "GFID-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalFreightInvoiceDigest(): Int {
    val policy = defaultPolicy("global-freight-invoice-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalFreightInvoiceDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
