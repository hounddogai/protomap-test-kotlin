package com.acme.transport.fleetops.tire.target

/** One step of the pending tire target calculation. */
interface PendingTireTargetFactor {
    fun describe(): String
    fun adjust(item: PendingTireTarget, value: Double): Double
}

data class PendingTireTargetBudgetOffsetAdjuster(var weight: Double) : PendingTireTargetFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PendingTireTarget, value: Double): Double = value + item.budget * weight
}

data class CapPendingTireTargetAdjuster(var cap: Double) : PendingTireTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTireTarget, value: Double): Double = minOf(value, cap)
}

data class LabelPendingTireTargetAdjuster(var factors: MutableMap<String, Double>) : PendingTireTargetFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingTireTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending tire target steps in order and keeps a trace of each. */
data class PendingTireTargetEngine(
    var steps: MutableList<PendingTireTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingTireTarget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTireTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTireTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingTireTargetEngine(): PendingTireTargetEngine {
    return PendingTireTargetEngine(
        steps = mutableListOf<PendingTireTargetFactor>(
            PendingTireTargetBudgetOffsetAdjuster(1.46),
            CapPendingTireTargetAdjuster(1467.0),
            LabelPendingTireTargetAdjuster(mutableMapOf<String, Double>("retail" to 1.38, "north" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 66845.5,
    )
}
