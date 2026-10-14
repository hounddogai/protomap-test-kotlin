package com.acme.transport.linehaul.tender.template

import com.acme.transport.linehaul.tender.recorditem.LenientTenderCheckpoint
import com.acme.transport.linehaul.tender.recorditem.classifyLenientTenderCheckpoint

fun summarizeLenientTenderCheckpointsForPreference(items: MutableList<LenientTenderCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePrimaryTenderPreferencesFromCheckpoint(items: MutableList<LenientTenderCheckpoint>): MutableList<PrimaryTenderPreference> {
    val out = mutableListOf<PrimaryTenderPreference>()
    for (item in items) {
        val derived = newPrimaryTenderPreference(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePrimaryTenderPreference(item: PrimaryTenderPreference): String {
    return classifyLenientTenderCheckpoint(item.effectiveValue())
}
