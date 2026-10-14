package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.linehaul.tender.tenderrollup.LegacyTenderVariance
import com.acme.transport.linehaul.tender.tenderrollup.scheduledTenderCycleBucket

fun summarizeLegacyTenderVariancesForLedger(items: MutableList<LegacyTenderVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingLanePlanLedgersFromVariance(items: MutableList<LegacyTenderVariance>): MutableList<RollingLanePlanLedger> {
    val out = mutableListOf<RollingLanePlanLedger>()
    for (item in items) {
        val derived = newRollingLanePlanLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRollingLanePlanLedger(item: RollingLanePlanLedger): String {
    return scheduledTenderCycleBucket(item.adjustedExposure())
}
