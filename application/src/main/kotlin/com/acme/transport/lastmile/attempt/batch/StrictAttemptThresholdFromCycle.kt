package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.parcel.cappedparcelcycle.PrimaryParcelCycle
import com.acme.transport.lastmile.parcel.cappedparcelcycle.classifyTieredParcelPreference

fun summarizePrimaryParcelCyclesForThreshold(items: MutableList<PrimaryParcelCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveStrictAttemptThresholdsFromCycle(items: MutableList<PrimaryParcelCycle>): MutableList<StrictAttemptThreshold> {
    val out = mutableListOf<StrictAttemptThreshold>()
    for (item in items) {
        val derived = newStrictAttemptThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateStrictAttemptThreshold(item: StrictAttemptThreshold): String {
    return classifyTieredParcelPreference(item.effectiveValue())
}
