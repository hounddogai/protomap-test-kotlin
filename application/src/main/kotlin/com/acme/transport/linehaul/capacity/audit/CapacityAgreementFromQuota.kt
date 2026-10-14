package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.linehaul.capacity.quota.CapacityQuota
import com.acme.transport.linehaul.capacity.quota.capacityQuotaBucket

fun summarizeCapacityQuotasForAgreement(items: MutableList<CapacityQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCapacityAgreementsFromQuota(items: MutableList<CapacityQuota>): MutableList<CapacityAgreement> {
    val out = mutableListOf<CapacityAgreement>()
    for (item in items) {
        val derived = newCapacityAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateCapacityAgreement(item: CapacityAgreement): String = capacityQuotaBucket(item.effectiveVolume())
