package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a freight invoice ledger code in its canonical FIL- form. */
fun normalizeFreightInvoiceLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIL-")) {
        return clean
    }
    return "FIL-${clean.replace(" ", "-")}"
}

fun freightInvoiceLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFreightInvoiceLedger(): Int {
    val policy = defaultPolicy("freight-invoice-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
