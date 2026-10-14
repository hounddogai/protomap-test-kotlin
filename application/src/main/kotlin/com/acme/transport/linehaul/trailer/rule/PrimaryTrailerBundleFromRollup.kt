package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.linehaul.tender.rollup.TenderRollup
import com.acme.transport.linehaul.tender.rollup.tenderRollupBucket

fun summarizeTenderRollupsForBundle(items: MutableList<TenderRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePrimaryTrailerBundlesFromRollup(items: MutableList<TenderRollup>): MutableList<PrimaryTrailerBundle> {
    val out = mutableListOf<PrimaryTrailerBundle>()
    for (item in items) {
        val derived = newPrimaryTrailerBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePrimaryTrailerBundle(item: PrimaryTrailerBundle): String = tenderRollupBucket(item.netVolume())
