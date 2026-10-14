package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.linehaul.capacity.audit.CapacityAgreement
import com.acme.transport.linehaul.capacity.audit.capacityAuditBucket

fun summarizeCapacityAgreementsForVariance(items: MutableList<CapacityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCapacityVariancesFromAgreement(items: MutableList<CapacityAgreement>): MutableList<CapacityVariance> {
    val out = mutableListOf<CapacityVariance>()
    for (item in items) {
        val derived = newCapacityVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCapacityVariance(item: CapacityVariance): String = capacityAuditBucket(item.effectiveValue())
