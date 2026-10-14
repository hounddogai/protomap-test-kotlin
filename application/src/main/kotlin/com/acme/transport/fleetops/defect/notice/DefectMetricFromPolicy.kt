package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.defect.policy.DefectPolicy
import com.acme.transport.fleetops.defect.policy.normalizeBatchedDefectProfileCode

fun summarizeDefectPoliciesForMetric(items: MutableList<DefectPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefectMetricsFromPolicy(items: MutableList<DefectPolicy>): MutableList<DefectMetric> {
    val out = mutableListOf<DefectMetric>()
    for (item in items) {
        val derived = newDefectMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefectMetric(item: DefectMetric): String = normalizeBatchedDefectProfileCode(item.code)
