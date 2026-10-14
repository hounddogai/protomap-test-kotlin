package com.acme.transport.network.node.score

/** One step of the pending node cycle calculation. */
interface PendingNodeCycleStep {
    fun describe(): String
    fun adjust(item: PendingNodeCycle, value: Double): Double
}

data class TitlePendingNodeCycleAdjuster(var factors: MutableMap<String, Double>) : PendingNodeCycleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingNodeCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingNodeCycleRevisionOffsetAdjuster(var weight: Double) : PendingNodeCycleStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PendingNodeCycle, value: Double): Double = value + item.revision.toDouble() * weight
}

data class PercentPendingNodeCycleAdjuster(var percent: Double) : PendingNodeCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingNodeCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingNodeCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingNodeCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingNodeCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingNodeCycleAdjuster(var cap: Double) : PendingNodeCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingNodeCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the pending node cycle steps in order and keeps a trace of each. */
data class PendingNodeCycleEngine(
    var steps: MutableList<PendingNodeCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingNodeCycle): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingNodeCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingNodeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingNodeCycleEngine(): PendingNodeCycleEngine {
    return PendingNodeCycleEngine(
        steps = mutableListOf<PendingNodeCycleStep>(
            TitlePendingNodeCycleAdjuster(mutableMapOf<String, Double>("west" to 1.1)),
            PendingNodeCycleRevisionOffsetAdjuster(1.06),
            PercentPendingNodeCycleAdjuster(-14.9),
            TieredPendingNodeCycleAdjuster(
                mutableListOf<Double>(717.0, 1368.0, 4527.0, 9391.0),
                mutableListOf<Double>(1.0, 0.89, 1.09, 0.89),
            ),
            CapPendingNodeCycleAdjuster(11524.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 19681.1,
    )
}
