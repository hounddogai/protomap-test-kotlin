package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.freightpay.audit.batch.DefaultAuditNotice
import com.acme.transport.freightpay.audit.batch.normalizeDefaultAuditNoticeCode

fun summarizeDefaultAuditNoticesForDraft(items: MutableList<DefaultAuditNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLenientFreightInvoiceDraftsFromNotice(items: MutableList<DefaultAuditNotice>): MutableList<LenientFreightInvoiceDraft> {
    val out = mutableListOf<LenientFreightInvoiceDraft>()
    for (item in items) {
        val derived = newLenientFreightInvoiceDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLenientFreightInvoiceDraft(item: LenientFreightInvoiceDraft): String {
    return normalizeDefaultAuditNoticeCode(item.code)
}
