package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.emissionfactor.draft.EmissionFactorDraft
import com.acme.transport.emissions.emissionfactor.draft.emissionFactorBudgetBucket

fun summarizeEmissionFactorDraftsForBatch(items: MutableList<EmissionFactorDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveEmissionFactorBatchesFromDraft(items: MutableList<EmissionFactorDraft>): MutableList<EmissionFactorBatch> {
    val out = mutableListOf<EmissionFactorBatch>()
    for (item in items) {
        val derived = newEmissionFactorBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateEmissionFactorBatch(item: EmissionFactorBatch): String = emissionFactorBudgetBucket(item.netVolume())
