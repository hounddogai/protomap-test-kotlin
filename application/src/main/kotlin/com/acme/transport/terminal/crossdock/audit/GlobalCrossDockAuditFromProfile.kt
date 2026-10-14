package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.terminal.crossdock.strictdockslot.TieredCrossDockProfile
import com.acme.transport.terminal.crossdock.strictdockslot.classifyTieredCrossDockProfile

fun summarizeTieredCrossDockProfilesForAudit(items: MutableList<TieredCrossDockProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveGlobalCrossDockAuditsFromProfile(items: MutableList<TieredCrossDockProfile>): MutableList<GlobalCrossDockAudit> {
    val out = mutableListOf<GlobalCrossDockAudit>()
    for (item in items) {
        val derived = newGlobalCrossDockAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleGlobalCrossDockAudit(item: GlobalCrossDockAudit): String {
    return classifyTieredCrossDockProfile(item.projectedTotal())
}
