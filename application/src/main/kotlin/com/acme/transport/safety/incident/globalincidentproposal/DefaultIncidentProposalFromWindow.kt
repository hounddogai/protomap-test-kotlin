package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.safety.scorecard.scorecardwindow.LenientScorecardWindow
import com.acme.transport.safety.scorecard.scorecardwindow.pendingScorecardPolicyBucket

fun summarizeLenientScorecardWindowsForProposal(items: MutableList<LenientScorecardWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultIncidentProposalsFromWindow(items: MutableList<LenientScorecardWindow>): MutableList<DefaultIncidentProposal> {
    val out = mutableListOf<DefaultIncidentProposal>()
    for (item in items) {
        val derived = newDefaultIncidentProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchDefaultIncidentProposal(item: DefaultIncidentProposal): String {
    return pendingScorecardPolicyBucket(item.netLoad())
}
