package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.freightpay.accessorial.audit.DefaultAccessorialRollup
import com.acme.transport.freightpay.accessorial.audit.normalizeCappedAccessorialAuditCode

fun summarizeDefaultAccessorialRollupsForAllocation(items: MutableList<DefaultAccessorialRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalAuditAllocationsFromRollup(items: MutableList<DefaultAccessorialRollup>): MutableList<RegionalAuditAllocation> {
    val out = mutableListOf<RegionalAuditAllocation>()
    for (item in items) {
        val derived = newRegionalAuditAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRegionalAuditAllocation(item: RegionalAuditAllocation): String {
    return normalizeCappedAccessorialAuditCode(item.code)
}
