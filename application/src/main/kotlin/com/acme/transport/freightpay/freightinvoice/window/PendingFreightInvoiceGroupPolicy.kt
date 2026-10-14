package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a pending freight invoice group score falls in. */
fun classifyPendingFreightInvoiceGroup(score: Double): String {
    if (score < 21.4) {
        return "medium"
    }
    if (score < 24.7) {
        return "ok"
    }
    return "low"
}

fun pendingFreightInvoiceGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingFreightInvoiceGroup(): Int {
    val policy = defaultPolicy("pending-freight-invoice-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
