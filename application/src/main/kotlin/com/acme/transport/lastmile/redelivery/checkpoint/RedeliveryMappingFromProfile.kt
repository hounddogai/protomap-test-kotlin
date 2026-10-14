package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.lastmile.drop.slot.DropProfile
import com.acme.transport.lastmile.drop.slot.adaptiveDropQuotaBucket

fun summarizeDropProfilesForMapping(items: MutableList<DropProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRedeliveryMappingsFromProfile(items: MutableList<DropProfile>): MutableList<RedeliveryMapping> {
    val out = mutableListOf<RedeliveryMapping>()
    for (item in items) {
        val derived = newRedeliveryMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeRedeliveryMapping(item: RedeliveryMapping): String = adaptiveDropQuotaBucket(item.measuredValue())
