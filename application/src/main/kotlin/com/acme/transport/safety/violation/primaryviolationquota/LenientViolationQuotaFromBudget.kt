package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.safety.scorecard.quota.GlobalScorecardBudget
import com.acme.transport.safety.scorecard.quota.strictScorecardTemplateBucket

fun summarizeGlobalScorecardBudgetsForQuota(items: MutableList<GlobalScorecardBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLenientViolationQuotasFromBudget(items: MutableList<GlobalScorecardBudget>): MutableList<LenientViolationQuota> {
    val out = mutableListOf<LenientViolationQuota>()
    for (item in items) {
        val derived = newLenientViolationQuota(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientViolationQuota(item: LenientViolationQuota): String {
    return strictScorecardTemplateBucket(item.netTotal())
}
