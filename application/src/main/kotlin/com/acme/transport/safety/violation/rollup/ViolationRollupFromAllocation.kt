package com.acme.transport.safety.violation.rollup

import com.acme.transport.safety.cameraevent.allocation.CameraEventAllocation
import com.acme.transport.safety.cameraevent.allocation.normalizeCameraEventAllocationCode

fun summarizeCameraEventAllocationsForRollup(items: MutableList<CameraEventAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveViolationRollupsFromAllocation(items: MutableList<CameraEventAllocation>): MutableList<ViolationRollup> {
    val out = mutableListOf<ViolationRollup>()
    for (item in items) {
        val derived = newViolationRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeViolationRollup(item: ViolationRollup): String = normalizeCameraEventAllocationCode(item.code)
