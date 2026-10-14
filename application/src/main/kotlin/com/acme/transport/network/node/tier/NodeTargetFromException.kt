package com.acme.transport.network.node.tier

import com.acme.transport.network.servicearea.cycle.BatchedServiceAreaException
import com.acme.transport.network.servicearea.cycle.batchedServiceAreaExceptionBucket

fun summarizeBatchedServiceAreaExceptionsForTarget(items: MutableList<BatchedServiceAreaException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveNodeTargetsFromException(items: MutableList<BatchedServiceAreaException>): MutableList<NodeTarget> {
    val out = mutableListOf<NodeTarget>()
    for (item in items) {
        val derived = newNodeTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveNodeTarget(item: NodeTarget): String = batchedServiceAreaExceptionBucket(item.effectiveValue())
