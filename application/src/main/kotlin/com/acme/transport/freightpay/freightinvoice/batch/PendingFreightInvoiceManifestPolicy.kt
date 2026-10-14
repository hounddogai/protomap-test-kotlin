package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingFreightInvoiceManifest(): Int {
    val policy = defaultPolicy("pending-freight-invoice-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingFreightInvoiceManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
