package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.restbreak.exception.AdaptiveRestBreakScore
import com.acme.transport.crewing.restbreak.exception.restBreakExceptionBucket

fun summarizeAdaptiveRestBreakScoresForQuota(items: MutableList<AdaptiveRestBreakScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePrimaryQualificationQuotasFromScore(items: MutableList<AdaptiveRestBreakScore>): MutableList<PrimaryQualificationQuota> {
    val out = mutableListOf<PrimaryQualificationQuota>()
    for (item in items) {
        val derived = newPrimaryQualificationQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun derivePrimaryQualificationQuota(item: PrimaryQualificationQuota): String {
    return restBreakExceptionBucket(item.projectedTotal())
}
