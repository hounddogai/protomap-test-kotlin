package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.dutyperiod.estimate.StrictDutyPeriodEstimate
import com.acme.transport.crewing.dutyperiod.estimate.dutyPeriodPlanWeight

fun summarizeStrictDutyPeriodEstimatesForPlan(items: MutableList<StrictDutyPeriodEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePrimaryAvailabilityPlansFromEstimate(items: MutableList<StrictDutyPeriodEstimate>): MutableList<PrimaryAvailabilityPlan> {
    val out = mutableListOf<PrimaryAvailabilityPlan>()
    for (item in items) {
        val derived = newPrimaryAvailabilityPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitPrimaryAvailabilityPlan(item: PrimaryAvailabilityPlan): Double = dutyPeriodPlanWeight(4, true)
