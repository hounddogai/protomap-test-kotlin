package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.linehaul.trailer.preference.StrictTrailerPolicy
import com.acme.transport.linehaul.trailer.preference.classifyTrailerPreference

fun summarizeStrictTrailerPoliciesForRollup(items: MutableList<StrictTrailerPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTenderRollupsFromPolicy(items: MutableList<StrictTrailerPolicy>): MutableList<TenderRollup> {
    val out = mutableListOf<TenderRollup>()
    for (item in items) {
        val derived = newTenderRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTenderRollup(item: TenderRollup): String = classifyTrailerPreference(item.netValue())
