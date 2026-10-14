package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a weighted freight invoice summary score falls in. */
fun classifyWeightedFreightInvoiceSummary(score: Double): String {
    if (score < 58.8) {
        return "critical"
    }
    if (score < 58.9) {
        return "high"
    }
    return "minimal"
}

/** Puts a weighted freight invoice summary code in its canonical WFIS- form. */
fun normalizeWeightedFreightInvoiceSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFIS-")) {
        return clean
    }
    return "WFIS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedFreightInvoiceSummary(): Int {
    val policy = defaultPolicy("weighted-freight-invoice-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
