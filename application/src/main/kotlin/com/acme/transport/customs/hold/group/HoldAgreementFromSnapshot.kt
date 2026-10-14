package com.acme.transport.customs.hold.group

import com.acme.transport.customs.declaration.snapshot.ManualDeclarationSnapshot
import com.acme.transport.customs.declaration.snapshot.normalizeStrictDeclarationBaselineCode

fun summarizeManualDeclarationSnapshotsForAgreement(items: MutableList<ManualDeclarationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldAgreementsFromSnapshot(items: MutableList<ManualDeclarationSnapshot>): MutableList<HoldAgreement> {
    val out = mutableListOf<HoldAgreement>()
    for (item in items) {
        val derived = newHoldAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeHoldAgreement(item: HoldAgreement): String = normalizeStrictDeclarationBaselineCode(item.code)
