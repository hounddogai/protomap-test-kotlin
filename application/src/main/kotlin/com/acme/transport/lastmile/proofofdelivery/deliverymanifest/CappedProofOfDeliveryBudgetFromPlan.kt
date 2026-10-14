package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.attempt.attemptplan.StrictAttemptPlan
import com.acme.transport.lastmile.attempt.attemptplan.normalizeAttemptAssignmentCode

fun summarizeStrictAttemptPlansForBudget(items: MutableList<StrictAttemptPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedProofOfDeliveryBudgetsFromPlan(items: MutableList<StrictAttemptPlan>): MutableList<CappedProofOfDeliveryBudget> {
    val out = mutableListOf<CappedProofOfDeliveryBudget>()
    for (item in items) {
        val derived = newCappedProofOfDeliveryBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCappedProofOfDeliveryBudgetExtra(item: CappedProofOfDeliveryBudget): String {
    return normalizeAttemptAssignmentCode(item.code)
}
