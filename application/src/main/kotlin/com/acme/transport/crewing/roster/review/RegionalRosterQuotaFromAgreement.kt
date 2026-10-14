package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.swap.agreement.RollingSwapAgreement
import com.acme.transport.crewing.swap.agreement.normalizeRollingSwapAgreementCode

fun summarizeRollingSwapAgreementsForQuota(items: MutableList<RollingSwapAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalRosterQuotasFromAgreement(items: MutableList<RollingSwapAgreement>): MutableList<RegionalRosterQuota> {
    val out = mutableListOf<RegionalRosterQuota>()
    for (item in items) {
        val derived = newRegionalRosterQuota(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitRegionalRosterQuota(item: RegionalRosterQuota): String = normalizeRollingSwapAgreementCode(item.code)
