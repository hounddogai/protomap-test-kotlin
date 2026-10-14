package com.acme.transport.linehaul.capacity.capacityassignment

/** One step of the capacity adjustment calculation. */
interface CapacityAdjustmentStep {
    fun describe(): String
    fun adjust(item: CapacityAdjustment, value: Double): Double
}

data class SourceCapacityAdjustmentAdjuster(var factors: MutableMap<String, Double>) : CapacityAdjustmentStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CapacityAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapacityAdjustmentVersionOffsetAdjuster(var weight: Double) : CapacityAdjustmentStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: CapacityAdjustment, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapCapacityAdjustmentAdjuster(var cap: Double) : CapacityAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCapacityAdjustmentAdjuster(var percent: Double) : CapacityAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capacity adjustment steps in order and keeps a trace of each. */
data class CapacityAdjustmentEngine(
    var steps: MutableList<CapacityAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityAdjustment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityAdjustmentEngine(): CapacityAdjustmentEngine {
    return CapacityAdjustmentEngine(
        steps = mutableListOf<CapacityAdjustmentStep>(
            SourceCapacityAdjustmentAdjuster(
                mutableMapOf<String, Double>("south" to 1.35, "central" to 1.1, "retail" to 0.74),
            ),
            CapacityAdjustmentVersionOffsetAdjuster(1.44),
            CapCapacityAdjustmentAdjuster(16764.0),
            TieredCapacityAdjustmentAdjuster(
                mutableListOf<Double>(1473.0, 9190.0),
                mutableListOf<Double>(1.11, 1.16),
            ),
            PercentCapacityAdjustmentAdjuster(17.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 39130.2,
    )
}
