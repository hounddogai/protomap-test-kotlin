package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.safety.violation.manifest.ViolationManifest
import com.acme.transport.safety.violation.manifest.adaptiveViolationAuditWeight

fun summarizeViolationManifestsForSchedule(items: MutableList<ViolationManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveStrictNearMissSchedulesFromManifest(items: MutableList<ViolationManifest>): MutableList<StrictNearMissSchedule> {
    val out = mutableListOf<StrictNearMissSchedule>()
    for (item in items) {
        val derived = newStrictNearMissSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyStrictNearMissSchedule(item: StrictNearMissSchedule): Double = adaptiveViolationAuditWeight(1, true)
