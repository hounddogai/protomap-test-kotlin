package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.roster.rule.RosterTemplate
import com.acme.transport.crewing.roster.rule.classifyRosterTemplate

fun summarizeRosterTemplatesForAgreement(items: MutableList<RosterTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveSwapAgreementsFromTemplate(items: MutableList<RosterTemplate>): MutableList<SwapAgreement> {
    val out = mutableListOf<SwapAgreement>()
    for (item in items) {
        val derived = newSwapAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifySwapAgreement(item: SwapAgreement): String = classifyRosterTemplate(item.adjustedExposure())
