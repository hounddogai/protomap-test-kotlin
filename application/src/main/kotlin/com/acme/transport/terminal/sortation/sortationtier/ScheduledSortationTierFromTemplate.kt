package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.terminal.sortation.sortationdigest.TieredSortationTemplate
import com.acme.transport.terminal.sortation.sortationdigest.classifyPendingSortationDigest

fun summarizeTieredSortationTemplatesForTier(items: MutableList<TieredSortationTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScheduledSortationTiersFromTemplate(items: MutableList<TieredSortationTemplate>): MutableList<ScheduledSortationTier> {
    val out = mutableListOf<ScheduledSortationTier>()
    for (item in items) {
        val derived = newScheduledSortationTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScheduledSortationTier(item: ScheduledSortationTier): String {
    return classifyPendingSortationDigest(item.projectedExposure())
}
