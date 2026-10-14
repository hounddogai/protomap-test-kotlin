package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalFreightInvoiceRule(): Int {
    val policy = defaultPolicy("seasonal-freight-invoice-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal freight invoice rule code in its canonical SFIR- form. */
fun normalizeSeasonalFreightInvoiceRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFIR-")) {
        return clean
    }
    return "SFIR-${clean.replace(" ", "-")}"
}
