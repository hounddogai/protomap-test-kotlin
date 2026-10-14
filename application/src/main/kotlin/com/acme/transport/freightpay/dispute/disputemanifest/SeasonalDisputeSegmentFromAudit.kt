package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.freightinvoice.threshold.FreightInvoiceAudit
import com.acme.transport.freightpay.freightinvoice.threshold.freightInvoiceAuditBucket

fun summarizeFreightInvoiceAuditsForSegment(items: MutableList<FreightInvoiceAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveSeasonalDisputeSegmentsFromAudit(items: MutableList<FreightInvoiceAudit>): MutableList<SeasonalDisputeSegment> {
    val out = mutableListOf<SeasonalDisputeSegment>()
    for (item in items) {
        val derived = newSeasonalDisputeSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeSeasonalDisputeSegment(item: SeasonalDisputeSegment): String {
    return freightInvoiceAuditBucket(item.netExposure())
}
