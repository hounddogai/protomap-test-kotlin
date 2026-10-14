package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.swap.window.AdaptiveSwapProposal
import com.acme.transport.crewing.swap.window.normalizeTieredSwapWindowCode

fun summarizeAdaptiveSwapProposalsForWindow(items: MutableList<AdaptiveSwapProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveQualificationWindowsFromProposal(items: MutableList<AdaptiveSwapProposal>): MutableList<QualificationWindow> {
    val out = mutableListOf<QualificationWindow>()
    for (item in items) {
        val derived = newQualificationWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreQualificationWindow(item: QualificationWindow): String = normalizeTieredSwapWindowCode(item.code)
