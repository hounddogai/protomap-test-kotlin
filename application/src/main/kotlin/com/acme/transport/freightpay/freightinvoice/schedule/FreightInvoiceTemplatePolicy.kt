package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 88890.0, in order. */
fun freightInvoiceTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88890.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFreightInvoiceTemplate(): Int {
    val policy = defaultPolicy("freight-invoice-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a freight invoice template code in its canonical FIT- form. */
fun normalizeFreightInvoiceTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIT-")) {
        return clean
    }
    return "FIT-${clean.replace(" ", "-")}"
}
