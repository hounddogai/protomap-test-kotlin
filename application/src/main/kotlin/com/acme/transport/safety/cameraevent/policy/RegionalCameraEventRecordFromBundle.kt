package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.safety.violation.slot.LegacyViolationBundle
import com.acme.transport.safety.violation.slot.classifyLegacyViolationBundle

fun summarizeLegacyViolationBundlesForRecord(items: MutableList<LegacyViolationBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalCameraEventRecordsFromBundle(items: MutableList<LegacyViolationBundle>): MutableList<RegionalCameraEventRecord> {
    val out = mutableListOf<RegionalCameraEventRecord>()
    for (item in items) {
        val derived = newRegionalCameraEventRecord(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalCameraEventRecord(item: RegionalCameraEventRecord): String {
    return classifyLegacyViolationBundle(item.adjustedValue())
}
