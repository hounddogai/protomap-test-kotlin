package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.freightpay.audit.slot.RollingAuditMapping
import com.acme.transport.freightpay.audit.slot.legacyAuditManifestWeight

fun summarizeRollingAuditMappingsForBatch(items: MutableList<RollingAuditMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultFreightInvoiceBatchesFromMapping(items: MutableList<RollingAuditMapping>): MutableList<DefaultFreightInvoiceBatch> {
    val out = mutableListOf<DefaultFreightInvoiceBatch>()
    for (item in items) {
        val derived = newDefaultFreightInvoiceBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateDefaultFreightInvoiceBatch(item: DefaultFreightInvoiceBatch): Double {
    return legacyAuditManifestWeight(3, true)
}
