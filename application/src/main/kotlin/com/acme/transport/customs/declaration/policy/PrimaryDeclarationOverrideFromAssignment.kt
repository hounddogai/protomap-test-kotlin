package com.acme.transport.customs.declaration.policy

import com.acme.transport.customs.declaration.rule.TieredDeclarationAssignment
import com.acme.transport.customs.declaration.rule.normalizeFallbackDeclarationRecordCode

fun summarizeTieredDeclarationAssignmentsForOverride(items: MutableList<TieredDeclarationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePrimaryDeclarationOverridesFromAssignment(items: MutableList<TieredDeclarationAssignment>): MutableList<PrimaryDeclarationOverride> {
    val out = mutableListOf<PrimaryDeclarationOverride>()
    for (item in items) {
        val derived = newPrimaryDeclarationOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPrimaryDeclarationOverride(item: PrimaryDeclarationOverride): String {
    return normalizeFallbackDeclarationRecordCode(item.code)
}
