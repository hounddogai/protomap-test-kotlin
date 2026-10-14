package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.linehaul.capacity.audit.CapacityAudit
import com.acme.transport.linehaul.capacity.audit.capacityAuditBucket

fun summarizeCapacityAuditsForThreshold(items: MutableList<CapacityAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLanePlanThresholdsFromAudit(items: MutableList<CapacityAudit>): MutableList<LanePlanThreshold> {
    val out = mutableListOf<LanePlanThreshold>()
    for (item in items) {
        val derived = newLanePlanThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLanePlanThreshold(item: LanePlanThreshold): String = capacityAuditBucket(item.projectedTotal())
