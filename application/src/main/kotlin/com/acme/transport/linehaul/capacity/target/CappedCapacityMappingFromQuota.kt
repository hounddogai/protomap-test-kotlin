package com.acme.transport.linehaul.capacity.target

import com.acme.transport.linehaul.laneplan.quota.ScheduledLanePlanQuota
import com.acme.transport.linehaul.laneplan.quota.scheduledLanePlanQuotaWeight

fun summarizeScheduledLanePlanQuotasForMapping(items: MutableList<ScheduledLanePlanQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedCapacityMappingsFromQuota(items: MutableList<ScheduledLanePlanQuota>): MutableList<CappedCapacityMapping> {
    val out = mutableListOf<CappedCapacityMapping>()
    for (item in items) {
        val derived = newCappedCapacityMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceCappedCapacityMapping(item: CappedCapacityMapping): Double = scheduledLanePlanQuotaWeight(7, true)
