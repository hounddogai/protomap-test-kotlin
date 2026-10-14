package com.acme.transport.freightpay.audit.policy

import com.acme.transport.freightpay.freightinvoice.rollup.FreightInvoiceRollup
import com.acme.transport.freightpay.freightinvoice.rollup.classifyFreightInvoiceRollup

fun summarizeFreightInvoiceRollupsForPolicy(items: MutableList<FreightInvoiceRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAuditPoliciesFromRollup(items: MutableList<FreightInvoiceRollup>): MutableList<AuditPolicy> {
    val out = mutableListOf<AuditPolicy>()
    for (item in items) {
        val derived = newAuditPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAuditPolicy(item: AuditPolicy): String = classifyFreightInvoiceRollup(item.adjustedTotal())
