package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.crewing.availability.availabilityscore.RollingAvailabilityAgreement
import com.acme.transport.crewing.availability.availabilityscore.primaryAvailabilityPlanWeight

fun summarizeRollingAvailabilityAgreementsForProposal(items: MutableList<RollingAvailabilityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBatchedDutyPeriodProposalsFromAgreement(items: MutableList<RollingAvailabilityAgreement>): MutableList<BatchedDutyPeriodProposal> {
    val out = mutableListOf<BatchedDutyPeriodProposal>()
    for (item in items) {
        val derived = newBatchedDutyPeriodProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignBatchedDutyPeriodProposal(item: BatchedDutyPeriodProposal): Double {
    return primaryAvailabilityPlanWeight(1, true)
}
