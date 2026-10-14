package com.acme.transport.network.cutoff.claim

import com.acme.transport.network.zone.quota.ZoneQuota
import com.acme.transport.network.zone.quota.zoneQuotaBucket

fun summarizeZoneQuotasForLedger(items: MutableList<ZoneQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCutoffLedgersFromQuota(items: MutableList<ZoneQuota>): MutableList<CutoffLedger> {
    val out = mutableListOf<CutoffLedger>()
    for (item in items) {
        val derived = newCutoffLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectCutoffLedger(item: CutoffLedger): String = zoneQuotaBucket(item.netLoad())
