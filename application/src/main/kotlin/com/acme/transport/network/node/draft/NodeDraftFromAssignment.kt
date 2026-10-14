package com.acme.transport.network.node.draft

import com.acme.transport.network.cutoff.segment.CutoffAssignment
import com.acme.transport.network.cutoff.segment.classifyCutoffPolicy

fun summarizeCutoffAssignmentsForDraft(items: MutableList<CutoffAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveNodeDraftsFromAssignment(items: MutableList<CutoffAssignment>): MutableList<NodeDraft> {
    val out = mutableListOf<NodeDraft>()
    for (item in items) {
        val derived = newNodeDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeNodeDraft(item: NodeDraft): String = classifyCutoffPolicy(item.effectiveExposure())
