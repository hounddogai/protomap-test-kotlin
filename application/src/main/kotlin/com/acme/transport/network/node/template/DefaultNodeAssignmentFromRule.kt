package com.acme.transport.network.node.template

import com.acme.transport.network.servicearea.areacycle.ScheduledServiceAreaRule
import com.acme.transport.network.servicearea.areacycle.scheduledServiceAreaRuleWeight

fun summarizeScheduledServiceAreaRulesForAssignment(items: MutableList<ScheduledServiceAreaRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultNodeAssignmentsFromRule(items: MutableList<ScheduledServiceAreaRule>): MutableList<DefaultNodeAssignment> {
    val out = mutableListOf<DefaultNodeAssignment>()
    for (item in items) {
        val derived = newDefaultNodeAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefaultNodeAssignment(item: DefaultNodeAssignment): Double = scheduledServiceAreaRuleWeight(4, true)
