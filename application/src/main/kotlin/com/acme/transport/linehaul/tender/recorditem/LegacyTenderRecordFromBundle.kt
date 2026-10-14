package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.linehaul.relay.bundle.GlobalRelayBundle
import com.acme.transport.linehaul.relay.bundle.classifyLenientRelaySummary

fun summarizeGlobalRelayBundlesForRecord(items: MutableList<GlobalRelayBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyTenderRecordsFromBundle(items: MutableList<GlobalRelayBundle>): MutableList<LegacyTenderRecord> {
    val out = mutableListOf<LegacyTenderRecord>()
    for (item in items) {
        val derived = newLegacyTenderRecord(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLegacyTenderRecord(item: LegacyTenderRecord): String = classifyLenientRelaySummary(item.weightedLoad())
