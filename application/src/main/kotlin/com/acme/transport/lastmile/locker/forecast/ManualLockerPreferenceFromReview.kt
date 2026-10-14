package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.lastmile.redelivery.redeliverywindow.TieredRedeliveryReview
import com.acme.transport.lastmile.redelivery.redeliverywindow.normalizeRedeliveryBudgetCode

fun summarizeTieredRedeliveryReviewsForPreference(items: MutableList<TieredRedeliveryReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveManualLockerPreferencesFromReview(items: MutableList<TieredRedeliveryReview>): MutableList<ManualLockerPreference> {
    val out = mutableListOf<ManualLockerPreference>()
    for (item in items) {
        val derived = newManualLockerPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeManualLockerPreference(item: ManualLockerPreference): String {
    return normalizeRedeliveryBudgetCode(item.code)
}
