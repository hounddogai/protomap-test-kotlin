package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun strictFreightInvoiceRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictFreightInvoiceRequest(): Int {
    val policy = defaultPolicy("strict-freight-invoice-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict freight invoice request code in its canonical SFIR- form. */
fun normalizeStrictFreightInvoiceRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFIR-")) {
        return clean
    }
    return "SFIR-${clean.replace(" ", "-")}"
}

fun strictFreightInvoiceRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
