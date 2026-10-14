package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped capacity mapping calculation. */
interface CappedCapacityMappingFactor {
    fun describe(): String
    fun adjust(item: CappedCapacityMapping, value: Double): Double
}

data class CappedCapacityMappingSequenceOffsetAdjuster(var weight: Double) : CappedCapacityMappingFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class MinimumCappedCapacityMappingAdjuster(var minimum: Double) : CappedCapacityMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedCapacityMappingAdjuster(var cap: Double) : CappedCapacityMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedCapacityMappingAdjuster(var step: Double) : CappedCapacityMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double = roundTo(value, step)
}

data class LabelCappedCapacityMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCapacityMappingFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedCapacityMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCapacityMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCapacityMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped capacity mapping steps in order and keeps a trace of each. */
data class CappedCapacityMappingEngine(
    var steps: MutableList<CappedCapacityMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedCapacityMapping): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCapacityMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedCapacityMappingEngine(): CappedCapacityMappingEngine {
    return CappedCapacityMappingEngine(
        steps = mutableListOf<CappedCapacityMappingFactor>(
            CappedCapacityMappingSequenceOffsetAdjuster(1.02),
            MinimumCappedCapacityMappingAdjuster(3.6),
            CapCappedCapacityMappingAdjuster(35981.0),
            RoundingCappedCapacityMappingAdjuster(0.05),
            LabelCappedCapacityMappingAdjuster(mutableMapOf<String, Double>("south" to 1.38, "central" to 0.89)),
            TieredCappedCapacityMappingAdjuster(
                mutableListOf<Double>(1280.0, 8955.0),
                mutableListOf<Double>(1.04, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 25426.8,
    )
}
