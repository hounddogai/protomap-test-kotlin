package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.freightpay.audit.lenientauditproposal.GlobalAuditProposal
import com.acme.transport.freightpay.audit.lenientauditproposal.normalizeAuditTierCode

fun summarizeGlobalAuditProposalsForSchedule(items: MutableList<GlobalAuditProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingFreightInvoiceSchedulesFromProposal(items: MutableList<GlobalAuditProposal>): MutableList<PendingFreightInvoiceSchedule> {
    val out = mutableListOf<PendingFreightInvoiceSchedule>()
    for (item in items) {
        val derived = newPendingFreightInvoiceSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scorePendingFreightInvoiceSchedule(item: PendingFreightInvoiceSchedule): String {
    return normalizeAuditTierCode(item.code)
}
