package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.scorecard.scorecardwindow.LenientScorecardWindow
import com.acme.transport.safety.scorecard.scorecardwindow.normalizeLenientScorecardWindowCode

fun summarizeLenientScorecardWindowsForEntry(items: MutableList<LenientScorecardWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictCoachingEntriesFromWindow(items: MutableList<LenientScorecardWindow>): MutableList<StrictCoachingEntry> {
    val out = mutableListOf<StrictCoachingEntry>()
    for (item in items) {
        val derived = newStrictCoachingEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictCoachingEntry(item: StrictCoachingEntry): String = normalizeLenientScorecardWindowCode(item.code)
