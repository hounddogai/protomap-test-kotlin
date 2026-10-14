package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFreightInvoiceDigest(): Int {
    val policy = defaultPolicy("freight-invoice-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 76398.0, in order. */
fun freightInvoiceDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76398.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun freightInvoiceDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a freight invoice digest code in its canonical FID- form. */
fun normalizeFreightInvoiceDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FID-")) {
        return clean
    }
    return "FID-${clean.replace(" ", "-")}"
}
