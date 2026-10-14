package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.linehaul.trailer.segment.ManualTrailerOverride
import com.acme.transport.linehaul.trailer.segment.normalizeCappedTrailerEntryCode

fun summarizeManualTrailerOverridesForRollup(items: MutableList<ManualTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTenderRollupsFromOverride(items: MutableList<ManualTrailerOverride>): MutableList<TieredTenderRollup> {
    val out = mutableListOf<TieredTenderRollup>()
    for (item in items) {
        val derived = newTieredTenderRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectTieredTenderRollup(item: TieredTenderRollup): String = normalizeCappedTrailerEntryCode(item.code)
