package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.safety.violation.variance.ViolationVariance
import com.acme.transport.safety.violation.variance.globalViolationBudgetWeight

fun summarizeViolationVariancesForGroup(items: MutableList<ViolationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCameraEventGroupsFromVariance(items: MutableList<ViolationVariance>): MutableList<CameraEventGroup> {
    val out = mutableListOf<CameraEventGroup>()
    for (item in items) {
        val derived = newCameraEventGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankCameraEventGroup(item: CameraEventGroup): Double = globalViolationBudgetWeight(2, true)
