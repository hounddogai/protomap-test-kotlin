package com.acme.transport.customs.clearance.metric

import com.acme.transport.customs.declaration.review.LenientDeclarationCheckpoint
import com.acme.transport.customs.declaration.review.classifyDeclarationCheckpoint

fun summarizeLenientDeclarationCheckpointsForMapping(items: MutableList<LenientDeclarationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveClearanceMappingsFromCheckpoint(items: MutableList<LenientDeclarationCheckpoint>): MutableList<ClearanceMapping> {
    val out = mutableListOf<ClearanceMapping>()
    for (item in items) {
        val derived = newClearanceMapping(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectClearanceMapping(item: ClearanceMapping): String {
    return classifyDeclarationCheckpoint(item.projectedExposure())
}
