package com.acme.transport.customs.declaration.baseline

import com.acme.transport.customs.declaration.manifest.DeclarationManifest
import com.acme.transport.customs.declaration.manifest.declarationManifestBucket

fun summarizeDeclarationManifestsForMilestone(items: MutableList<DeclarationManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDeclarationMilestonesFromManifest(items: MutableList<DeclarationManifest>): MutableList<DeclarationMilestone> {
    val out = mutableListOf<DeclarationMilestone>()
    for (item in items) {
        val derived = newDeclarationMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDeclarationMilestone(item: DeclarationMilestone): String {
    return declarationManifestBucket(item.projectedVolume())
}
