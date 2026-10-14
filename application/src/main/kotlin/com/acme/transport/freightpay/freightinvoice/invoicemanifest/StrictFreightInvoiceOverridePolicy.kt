package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun strictFreightInvoiceOverrideBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a strict freight invoice override code in its canonical SFIO- form. */
fun normalizeStrictFreightInvoiceOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFIO-")) {
        return clean
    }
    return "SFIO-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictFreightInvoiceOverride(): Int {
    val policy = defaultPolicy("strict-freight-invoice-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
