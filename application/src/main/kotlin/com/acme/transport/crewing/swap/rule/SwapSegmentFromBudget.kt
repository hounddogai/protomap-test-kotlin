package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.dutyperiod.periodnotice.DutyPeriodBudget
import com.acme.transport.crewing.dutyperiod.periodnotice.tieredDutyPeriodAgreementBucket

fun summarizeDutyPeriodBudgetsForSegment(items: MutableList<DutyPeriodBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveSwapSegmentsFromBudget(items: MutableList<DutyPeriodBudget>): MutableList<SwapSegment> {
    val out = mutableListOf<SwapSegment>()
    for (item in items) {
        val derived = newSwapSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeSwapSegment(item: SwapSegment): String = tieredDutyPeriodAgreementBucket(item.weightedLoad())
