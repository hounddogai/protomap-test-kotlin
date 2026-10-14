package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.accessorial.template.AccessorialAgreement
import com.acme.transport.freightpay.accessorial.template.accessorialRollupBucket

fun summarizeAccessorialAgreementsForBudget(items: MutableList<AccessorialAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualAccessorialBudgetsFromAgreement(items: MutableList<AccessorialAgreement>): MutableList<ManualAccessorialBudget> {
    val out = mutableListOf<ManualAccessorialBudget>()
    for (item in items) {
        val derived = newManualAccessorialBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleManualAccessorialBudget(item: ManualAccessorialBudget): String {
    return accessorialRollupBucket(item.effectiveVolume())
}
