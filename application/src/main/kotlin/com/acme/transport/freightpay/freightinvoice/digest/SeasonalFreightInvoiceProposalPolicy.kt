package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeSeasonalFreightInvoiceProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a seasonal freight invoice proposal code in its canonical SFIP- form. */
fun normalizeSeasonalFreightInvoiceProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFIP-")) {
        return clean
    }
    return "SFIP-${clean.replace(" ", "-")}"
}

fun seasonalFreightInvoiceProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalFreightInvoiceProposal(): Int {
    val policy = defaultPolicy("seasonal-freight-invoice-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
