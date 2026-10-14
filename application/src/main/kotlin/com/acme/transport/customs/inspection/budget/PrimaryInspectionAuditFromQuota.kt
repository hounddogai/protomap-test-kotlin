package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.broker.quota.DefaultBrokerQuota
import com.acme.transport.customs.broker.quota.classifyDefaultBrokerQuota

fun summarizeDefaultBrokerQuotasForAudit(items: MutableList<DefaultBrokerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun derivePrimaryInspectionAuditsFromQuota(items: MutableList<DefaultBrokerQuota>): MutableList<PrimaryInspectionAudit> {
    val out = mutableListOf<PrimaryInspectionAudit>()
    for (item in items) {
        val derived = newPrimaryInspectionAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryInspectionAudit(item: PrimaryInspectionAudit): String {
    return classifyDefaultBrokerQuota(item.measuredValue())
}
