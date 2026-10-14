package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.lastmile.locker.lockerbudget.SeasonalLockerRule
import com.acme.transport.lastmile.locker.lockerbudget.seasonalLockerRuleWeight

fun summarizeSeasonalLockerRulesForReview(items: MutableList<SeasonalLockerRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredRedeliveryReviewsFromRule(items: MutableList<SeasonalLockerRule>): MutableList<TieredRedeliveryReview> {
    val out = mutableListOf<TieredRedeliveryReview>()
    for (item in items) {
        val derived = newTieredRedeliveryReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTieredRedeliveryReview(item: TieredRedeliveryReview): Double = seasonalLockerRuleWeight(1, true)
