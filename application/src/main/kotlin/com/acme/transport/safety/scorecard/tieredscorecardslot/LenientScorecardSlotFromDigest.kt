package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.safety.coaching.segment.CappedCoachingDigest
import com.acme.transport.safety.coaching.segment.normalizeCappedCoachingDigestCode

fun summarizeCappedCoachingDigestsForSlot(items: MutableList<CappedCoachingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientScorecardSlotsFromDigest(items: MutableList<CappedCoachingDigest>): MutableList<LenientScorecardSlot> {
    val out = mutableListOf<LenientScorecardSlot>()
    for (item in items) {
        val derived = newLenientScorecardSlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientScorecardSlot(item: LenientScorecardSlot): String {
    return normalizeCappedCoachingDigestCode(item.code)
}
