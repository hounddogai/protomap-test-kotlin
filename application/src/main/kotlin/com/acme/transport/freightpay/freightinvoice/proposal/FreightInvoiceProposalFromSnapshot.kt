package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.freightpay.accessorial.accessorialscore.AccessorialSnapshot
import com.acme.transport.freightpay.accessorial.accessorialscore.classifySeasonalAccessorialMetric

fun summarizeAccessorialSnapshotsForProposal(items: MutableList<AccessorialSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveFreightInvoiceProposalsFromSnapshot(items: MutableList<AccessorialSnapshot>): MutableList<FreightInvoiceProposal> {
    val out = mutableListOf<FreightInvoiceProposal>()
    for (item in items) {
        val derived = newFreightInvoiceProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveFreightInvoiceProposal(item: FreightInvoiceProposal): String {
    return classifySeasonalAccessorialMetric(item.adjustedVolume())
}
