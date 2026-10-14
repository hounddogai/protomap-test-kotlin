package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.linehaul.laneplan.agreement.LanePlanBundle
import com.acme.transport.linehaul.laneplan.agreement.classifyLanePlanBundle

fun summarizeLanePlanBundlesForBatch(items: MutableList<LanePlanBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBackhaulBatchesFromBundle(items: MutableList<LanePlanBundle>): MutableList<BackhaulBatch> {
    val out = mutableListOf<BackhaulBatch>()
    for (item in items) {
        val derived = newBackhaulBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBackhaulBatch(item: BackhaulBatch): String = classifyLanePlanBundle(item.adjustedVolume())
