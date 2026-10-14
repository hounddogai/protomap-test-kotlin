package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.lastmile.parcel.profile.PrimaryParcelDraft
import com.acme.transport.lastmile.parcel.profile.normalizeGlobalParcelProfileCode

fun summarizePrimaryParcelDraftsForOverride(items: MutableList<PrimaryParcelDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDropOverridesFromDraft(items: MutableList<PrimaryParcelDraft>): MutableList<DropOverride> {
    val out = mutableListOf<DropOverride>()
    for (item in items) {
        val derived = newDropOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveDropOverride(item: DropOverride): String = normalizeGlobalParcelProfileCode(item.code)
