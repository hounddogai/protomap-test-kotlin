package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node review calculation. */
interface NodeReviewStep {
    fun describe(): String
    fun adjust(item: NodeReview, value: Double): Double
}

data class RoundingNodeReviewAdjuster(var step: Double) : NodeReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedNodeReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapNodeReviewAdjuster(var cap: Double) : NodeReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeReview, value: Double): Double = minOf(value, cap)
}

/** Runs the node review steps in order and keeps a trace of each. */
data class NodeReviewEngine(
    var steps: MutableList<NodeReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeReview): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<NodeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeReviewEngine(flags: Registry): NodeReviewEngine {
    return NodeReviewEngine(
        steps = mutableListOf<NodeReviewStep>(
            RoundingNodeReviewAdjuster(1.0),
            FlaggedNodeReviewAdjuster(flags, "node-review-rolling", -1.3),
            CapNodeReviewAdjuster(12926.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 84219.9,
    )
}
