package com.acme.transport.lastmile.parcel.review

import com.acme.transport.lastmile.attempt.manifest.StrictAttemptRecord
import com.acme.transport.lastmile.attempt.manifest.classifyAttemptEstimate

fun summarizeStrictAttemptRecordsForThreshold(items: MutableList<StrictAttemptRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveWeightedParcelThresholdsFromRecord(items: MutableList<StrictAttemptRecord>): MutableList<WeightedParcelThreshold> {
    val out = mutableListOf<WeightedParcelThreshold>()
    for (item in items) {
        val derived = newWeightedParcelThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceWeightedParcelThreshold(item: WeightedParcelThreshold): String {
    return classifyAttemptEstimate(item.effectiveValue())
}
