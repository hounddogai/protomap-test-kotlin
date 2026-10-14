package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.safety.nearmiss.audit.DefaultNearMissAudit
import com.acme.transport.safety.nearmiss.audit.normalizePrimaryNearMissPolicyCode

fun summarizeDefaultNearMissAuditsForCycle(items: MutableList<DefaultNearMissAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictCameraEventCyclesFromAudit(items: MutableList<DefaultNearMissAudit>): MutableList<StrictCameraEventCycle> {
    val out = mutableListOf<StrictCameraEventCycle>()
    for (item in items) {
        val derived = newStrictCameraEventCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictCameraEventCycle(item: StrictCameraEventCycle): String {
    return normalizePrimaryNearMissPolicyCode(item.code)
}
