package com.acme.transport.network.cutoff.estimate

import com.acme.transport.network.cutoff.scheduledcutoffclaim.LegacyCutoffBatch
import com.acme.transport.network.cutoff.scheduledcutoffclaim.normalizeScheduledCutoffPreferenceCode

fun summarizeLegacyCutoffBatchesForBatch(items: MutableList<LegacyCutoffBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryCutoffBatchesFromBatch(items: MutableList<LegacyCutoffBatch>): MutableList<PrimaryCutoffBatch> {
    val out = mutableListOf<PrimaryCutoffBatch>()
    for (item in items) {
        val derived = newPrimaryCutoffBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balancePrimaryCutoffBatch(item: PrimaryCutoffBatch): String {
    return normalizeScheduledCutoffPreferenceCode(item.code)
}
