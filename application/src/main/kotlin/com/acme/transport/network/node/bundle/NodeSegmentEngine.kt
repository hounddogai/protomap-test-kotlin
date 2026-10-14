package com.acme.transport.network.node.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the node segment calculation. */
interface NodeSegmentModifier {
    fun describe(): String
    fun adjust(item: NodeSegment, value: Double): Double
}

data class CapNodeSegmentAdjuster(var cap: Double) : NodeSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingNodeSegmentAdjuster(var step: Double) : NodeSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeSegment, value: Double): Double = roundTo(value, step)
}

data class NodeSegmentRevisionOffsetAdjuster(var weight: Double) : NodeSegmentModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: NodeSegment, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the node segment steps in order and keeps a trace of each. */
data class NodeSegmentEngine(
    var steps: MutableList<NodeSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeSegment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeSegmentModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeSegmentEngine(): NodeSegmentEngine {
    return NodeSegmentEngine(
        steps = mutableListOf<NodeSegmentModifier>(
            CapNodeSegmentAdjuster(12526.0),
            RoundingNodeSegmentAdjuster(0.5),
            NodeSegmentRevisionOffsetAdjuster(0.65),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 92368.4,
    )
}
