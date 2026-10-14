package com.acme.transport.customs.hold.bundle

import com.acme.transport.customs.tariff.entry.TariffDigest
import com.acme.transport.customs.tariff.entry.normalizeTariffEntryCode

fun summarizeTariffDigestsForDraft(items: MutableList<TariffDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveHoldDraftsFromDigest(items: MutableList<TariffDigest>): MutableList<HoldDraft> {
    val out = mutableListOf<HoldDraft>()
    for (item in items) {
        val derived = newHoldDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchHoldDraft(item: HoldDraft): String = normalizeTariffEntryCode(item.code)
