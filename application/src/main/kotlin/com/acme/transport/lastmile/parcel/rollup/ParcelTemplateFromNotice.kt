package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.lastmile.drop.batcheddropnotice.WeightedDropNotice
import com.acme.transport.lastmile.drop.batcheddropnotice.normalizeDropTemplateCode

fun summarizeWeightedDropNoticesForTemplate(items: MutableList<WeightedDropNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveParcelTemplatesFromNotice(items: MutableList<WeightedDropNotice>): MutableList<ParcelTemplate> {
    val out = mutableListOf<ParcelTemplate>()
    for (item in items) {
        val derived = newParcelTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveParcelTemplate(item: ParcelTemplate): String = normalizeDropTemplateCode(item.code)
