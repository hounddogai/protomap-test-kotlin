package com.acme.transport.network.hub.metric

import com.acme.transport.network.hub.audit.ScheduledHubAudit
import com.acme.transport.network.hub.audit.normalizeScheduledHubAuditCode

fun summarizeScheduledHubAuditsForTarget(items: MutableList<ScheduledHubAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryHubTargetsFromAudit(items: MutableList<ScheduledHubAudit>): MutableList<PrimaryHubTarget> {
    val out = mutableListOf<PrimaryHubTarget>()
    for (item in items) {
        val derived = newPrimaryHubTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePrimaryHubTarget(item: PrimaryHubTarget): String = normalizeScheduledHubAuditCode(item.code)
