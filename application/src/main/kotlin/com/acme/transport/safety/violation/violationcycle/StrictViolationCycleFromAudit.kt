package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.safety.cameraevent.globaleventaudit.LegacyCameraEventAudit
import com.acme.transport.safety.cameraevent.globaleventaudit.classifyBatchedCameraEventAllocation

fun summarizeLegacyCameraEventAuditsForCycle(items: MutableList<LegacyCameraEventAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictViolationCyclesFromAudit(items: MutableList<LegacyCameraEventAudit>): MutableList<StrictViolationCycle> {
    val out = mutableListOf<StrictViolationCycle>()
    for (item in items) {
        val derived = newStrictViolationCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectStrictViolationCycle(item: StrictViolationCycle): String {
    return classifyBatchedCameraEventAllocation(item.measuredValue())
}
