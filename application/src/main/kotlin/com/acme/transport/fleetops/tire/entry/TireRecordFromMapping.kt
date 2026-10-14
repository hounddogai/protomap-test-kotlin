package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.serviceplan.ledger.AdaptiveServicePlanMapping
import com.acme.transport.fleetops.serviceplan.ledger.classifyPrimaryServicePlanLedger

fun summarizeAdaptiveServicePlanMappingsForRecord(items: MutableList<AdaptiveServicePlanMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTireRecordsFromMapping(items: MutableList<AdaptiveServicePlanMapping>): MutableList<TireRecord> {
    val out = mutableListOf<TireRecord>()
    for (item in items) {
        val derived = newTireRecord(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTireRecord(item: TireRecord): String = classifyPrimaryServicePlanLedger(item.netTotal())
