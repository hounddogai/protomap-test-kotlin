package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.linehaul.trailer.trailerpreference.PrimaryTrailerNotice
import com.acme.transport.linehaul.trailer.trailerpreference.regionalTrailerPreferenceWeight

fun summarizePrimaryTrailerNoticesForProposal(items: MutableList<PrimaryTrailerNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalTenderProposalsFromNotice(items: MutableList<PrimaryTrailerNotice>): MutableList<SeasonalTenderProposal> {
    val out = mutableListOf<SeasonalTenderProposal>()
    for (item in items) {
        val derived = newSeasonalTenderProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalTenderProposal(item: SeasonalTenderProposal): Double {
    return regionalTrailerPreferenceWeight(3, true)
}
