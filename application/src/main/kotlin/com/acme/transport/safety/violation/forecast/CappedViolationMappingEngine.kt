package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped violation mapping calculation. */
interface CappedViolationMappingAdjuster {
    fun describe(): String
    fun adjust(item: CappedViolationMapping, value: Double): Double
}

data class MinimumCappedViolationMappingAdjuster(var minimum: Double) : CappedViolationMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedViolationMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedViolationMappingAdjuster(var step: Double) : CappedViolationMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedViolationMapping, value: Double): Double = roundTo(value, step)
}

data class CappedViolationMappingRateOffsetAdjuster(var weight: Double) : CappedViolationMappingAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: CappedViolationMapping, value: Double): Double = value + item.rate * weight
}

data class CapCappedViolationMappingAdjuster(var cap: Double) : CappedViolationMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedViolationMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the capped violation mapping steps in order and keeps a trace of each. */
data class CappedViolationMappingEngine(
    var steps: MutableList<CappedViolationMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedViolationMapping): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedViolationMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedViolationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedViolationMappingEngine(): CappedViolationMappingEngine {
    return CappedViolationMappingEngine(
        steps = mutableListOf<CappedViolationMappingAdjuster>(
            MinimumCappedViolationMappingAdjuster(26.9),
            RoundingCappedViolationMappingAdjuster(0.25),
            CappedViolationMappingRateOffsetAdjuster(0.97),
            CapCappedViolationMappingAdjuster(13152.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 75003.2,
    )
}
