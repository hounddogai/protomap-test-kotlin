package com.acme.transport.safety.violation.batch

import com.acme.transport.safety.violation.violationslot.StrictViolationMilestone
import com.acme.transport.safety.violation.violationslot.normalizeScheduledViolationTemplateCode

fun summarizeStrictViolationMilestonesForCycle(items: MutableList<StrictViolationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLenientViolationCyclesFromMilestone(items: MutableList<StrictViolationMilestone>): MutableList<LenientViolationCycle> {
    val out = mutableListOf<LenientViolationCycle>()
    for (item in items) {
        val derived = newLenientViolationCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLenientViolationCycle(item: LenientViolationCycle): String {
    return normalizeScheduledViolationTemplateCode(item.code)
}
