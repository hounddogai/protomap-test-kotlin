package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.emissionfactor.recorditem.EmissionFactorRecord
import com.acme.transport.emissions.emissionfactor.recorditem.normalizeStrictEmissionFactorAllocationCode

fun summarizeEmissionFactorRecordsForRollup(items: MutableList<EmissionFactorRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultIdlingRollupsFromRecord(items: MutableList<EmissionFactorRecord>): MutableList<DefaultIdlingRollup> {
    val out = mutableListOf<DefaultIdlingRollup>()
    for (item in items) {
        val derived = newDefaultIdlingRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefaultIdlingRollup(item: DefaultIdlingRollup): String {
    return normalizeStrictEmissionFactorAllocationCode(item.code)
}
