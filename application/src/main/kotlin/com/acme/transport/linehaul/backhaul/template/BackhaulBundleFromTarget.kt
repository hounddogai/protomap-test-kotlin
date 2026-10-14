package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.tender.baseline.TenderTarget
import com.acme.transport.linehaul.tender.baseline.tenderBaselineBucket

fun summarizeTenderTargetsForBundle(items: MutableList<TenderTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBackhaulBundlesFromTarget(items: MutableList<TenderTarget>): MutableList<BackhaulBundle> {
    val out = mutableListOf<BackhaulBundle>()
    for (item in items) {
        val derived = newBackhaulBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBackhaulBundle(item: BackhaulBundle): String = tenderBaselineBucket(item.netVolume())
