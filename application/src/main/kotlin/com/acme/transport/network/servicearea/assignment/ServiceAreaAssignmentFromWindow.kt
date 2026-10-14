package com.acme.transport.network.servicearea.assignment

import com.acme.transport.network.node.policy.NodeWindow
import com.acme.transport.network.node.policy.normalizeNodeCheckpointCode

fun summarizeNodeWindowsForAssignment(items: MutableList<NodeWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServiceAreaAssignmentsFromWindow(items: MutableList<NodeWindow>): MutableList<ServiceAreaAssignment> {
    val out = mutableListOf<ServiceAreaAssignment>()
    for (item in items) {
        val derived = newServiceAreaAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceServiceAreaAssignment(item: ServiceAreaAssignment): String = normalizeNodeCheckpointCode(item.code)
