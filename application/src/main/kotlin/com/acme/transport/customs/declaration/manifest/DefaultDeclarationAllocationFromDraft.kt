package com.acme.transport.customs.declaration.manifest

import com.acme.transport.customs.hold.bundle.HoldDraft
import com.acme.transport.customs.hold.bundle.classifyHoldDraft

fun summarizeHoldDraftsForAllocation(items: MutableList<HoldDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDefaultDeclarationAllocationsFromDraft(items: MutableList<HoldDraft>): MutableList<DefaultDeclarationAllocation> {
    val out = mutableListOf<DefaultDeclarationAllocation>()
    for (item in items) {
        val derived = newDefaultDeclarationAllocation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchDefaultDeclarationAllocation(item: DefaultDeclarationAllocation): String {
    return classifyHoldDraft(item.effectiveValue())
}
