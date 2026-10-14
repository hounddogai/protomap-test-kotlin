package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.lastmile.redelivery.snapshot.RegionalRedeliveryPreference
import com.acme.transport.lastmile.redelivery.snapshot.seasonalRedeliveryAuditBucket

fun summarizeRegionalRedeliveryPreferencesForMapping(items: MutableList<RegionalRedeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDropMappingsFromPreference(items: MutableList<RegionalRedeliveryPreference>): MutableList<DropMapping> {
    val out = mutableListOf<DropMapping>()
    for (item in items) {
        val derived = newDropMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignDropMapping(item: DropMapping): String = seasonalRedeliveryAuditBucket(item.effectiveExposure())
