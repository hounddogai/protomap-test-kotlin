package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.freightinvoice.manifest.FreightInvoiceManifest
import com.acme.transport.freightpay.freightinvoice.manifest.normalizeFreightInvoiceCheckpointCode

fun summarizeFreightInvoiceManifestsForSlot(items: MutableList<FreightInvoiceManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAuditSlotsFromManifest(items: MutableList<FreightInvoiceManifest>): MutableList<AuditSlot> {
    val out = mutableListOf<AuditSlot>()
    for (item in items) {
        val derived = newAuditSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAuditSlotExtra(item: AuditSlot): String = normalizeFreightInvoiceCheckpointCode(item.code)
