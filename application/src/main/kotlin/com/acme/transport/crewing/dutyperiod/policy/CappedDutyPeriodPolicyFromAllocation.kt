package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.qualification.plan.QualificationAllocation
import com.acme.transport.crewing.qualification.plan.classifyQualificationAllocation

fun summarizeQualificationAllocationsForPolicy(items: MutableList<QualificationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedDutyPeriodPoliciesFromAllocation(items: MutableList<QualificationAllocation>): MutableList<CappedDutyPeriodPolicy> {
    val out = mutableListOf<CappedDutyPeriodPolicy>()
    for (item in items) {
        val derived = newCappedDutyPeriodPolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeCappedDutyPeriodPolicy(item: CappedDutyPeriodPolicy): String {
    return classifyQualificationAllocation(item.projectedValue())
}
