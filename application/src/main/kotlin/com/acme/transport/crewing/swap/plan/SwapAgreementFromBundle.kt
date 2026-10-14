package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.dutyperiod.snapshot.DutyPeriodBundle
import com.acme.transport.crewing.dutyperiod.snapshot.dutyPeriodProfileWeight

fun summarizeDutyPeriodBundlesForAgreement(items: MutableList<DutyPeriodBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveSwapAgreementsFromBundle(items: MutableList<DutyPeriodBundle>): MutableList<SwapAgreement> {
    val out = mutableListOf<SwapAgreement>()
    for (item in items) {
        val derived = newSwapAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeSwapAgreement(item: SwapAgreement): Double = dutyPeriodProfileWeight(3, true)
