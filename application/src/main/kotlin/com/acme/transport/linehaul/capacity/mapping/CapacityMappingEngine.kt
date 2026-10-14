package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity mapping calculation. */
interface CapacityMappingStep {
    fun describe(): String
    fun adjust(item: CapacityMapping, value: Double): Double
}

data class PercentCapacityMappingAdjuster(var percent: Double) : CapacityMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapacityMappingMinimumOffsetAdjuster(var weight: Double) : CapacityMappingStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: CapacityMapping, value: Double): Double = value + item.minimum * weight
}

data class TieredCapacityMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCapacityMappingAdjuster(var minimum: Double) : CapacityMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCapacityMappingAdjuster(var step: Double) : CapacityMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityMapping, value: Double): Double = roundTo(value, step)
}

data class CapCapacityMappingAdjuster(var cap: Double) : CapacityMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the capacity mapping steps in order and keeps a trace of each. */
data class CapacityMappingEngine(
    var steps: MutableList<CapacityMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityMapping): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityMappingEngine(): CapacityMappingEngine {
    return CapacityMappingEngine(
        steps = mutableListOf<CapacityMappingStep>(
            PercentCapacityMappingAdjuster(-11.8),
            CapacityMappingMinimumOffsetAdjuster(1.74),
            TieredCapacityMappingAdjuster(
                mutableListOf<Double>(1519.0, 6175.0, 8490.0, 8858.0),
                mutableListOf<Double>(1.02, 1.17, 1.11, 0.95),
            ),
            MinimumCapacityMappingAdjuster(39.7),
            RoundingCapacityMappingAdjuster(0.5),
            CapCapacityMappingAdjuster(16092.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 94754.4,
    )
}
