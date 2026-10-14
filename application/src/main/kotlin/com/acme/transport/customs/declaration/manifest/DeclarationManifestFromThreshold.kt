package com.acme.transport.customs.declaration.manifest

import com.acme.transport.customs.inspection.inspectionmilestone.InspectionThreshold
import com.acme.transport.customs.inspection.inspectionmilestone.classifyInspectionThreshold

fun summarizeInspectionThresholdsForManifest(items: MutableList<InspectionThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveDeclarationManifestsFromThreshold(items: MutableList<InspectionThreshold>): MutableList<DeclarationManifest> {
    val out = mutableListOf<DeclarationManifest>()
    for (item in items) {
        val derived = newDeclarationManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDeclarationManifest(item: DeclarationManifest): String {
    return classifyInspectionThreshold(item.measuredValue())
}
