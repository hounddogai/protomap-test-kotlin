package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.linehaul.trailer.scheduledtrailerbatch.WeightedTrailerBatch
import com.acme.transport.linehaul.trailer.scheduledtrailerbatch.batchedTrailerScoreWeight

fun summarizeWeightedTrailerBatchesForSlot(items: MutableList<WeightedTrailerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLenientLanePlanSlotsFromBatch(items: MutableList<WeightedTrailerBatch>): MutableList<LenientLanePlanSlot> {
    val out = mutableListOf<LenientLanePlanSlot>()
    for (item in items) {
        val derived = newLenientLanePlanSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLenientLanePlanSlot(item: LenientLanePlanSlot): Double = batchedTrailerScoreWeight(7, true)
