package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional capacity mapping calculation. */
interface RegionalCapacityMappingStep {
    fun describe(): String
    fun adjust(item: RegionalCapacityMapping, value: Double): Double
}

data class CapRegionalCapacityMappingAdjuster(var cap: Double) : RegionalCapacityMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double = minOf(value, cap)
}

data class RegionalCapacityMappingThresholdOffsetAdjuster(var weight: Double) : RegionalCapacityMappingStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double = value + item.threshold * weight
}

data class SourceRegionalCapacityMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCapacityMappingStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalCapacityMappingAdjuster(var percent: Double) : RegionalCapacityMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalCapacityMappingAdjuster(var step: Double) : RegionalCapacityMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalCapacityMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCapacityMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCapacityMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional capacity mapping steps in order and keeps a trace of each. */
data class RegionalCapacityMappingEngine(
    var steps: MutableList<RegionalCapacityMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCapacityMapping): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCapacityMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCapacityMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCapacityMappingEngine(): RegionalCapacityMappingEngine {
    return RegionalCapacityMappingEngine(
        steps = mutableListOf<RegionalCapacityMappingStep>(
            CapRegionalCapacityMappingAdjuster(5181.0),
            RegionalCapacityMappingThresholdOffsetAdjuster(0.87),
            SourceRegionalCapacityMappingAdjuster(mutableMapOf<String, Double>("online" to 0.92)),
            PercentRegionalCapacityMappingAdjuster(-21.4),
            RoundingRegionalCapacityMappingAdjuster(0.25),
            TieredRegionalCapacityMappingAdjuster(
                mutableListOf<Double>(6648.0, 6967.0),
                mutableListOf<Double>(0.84, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 14789.2,
    )
}
