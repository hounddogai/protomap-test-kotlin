package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity cycle calculation. */
interface CapacityCycleFactor {
    fun describe(): String
    fun adjust(item: CapacityCycle, value: Double): Double
}

data class TieredCapacityCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCapacityCycleAdjuster(var minimum: Double) : CapacityCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapacityCycleExpiresAtOffsetAdjuster(var weight: Double) : CapacityCycleFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CapacityCycle, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingCapacityCycleAdjuster(var step: Double) : CapacityCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity cycle steps in order and keeps a trace of each. */
data class CapacityCycleEngine(
    var steps: MutableList<CapacityCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityCycle): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityCycleEngine(): CapacityCycleEngine {
    return CapacityCycleEngine(
        steps = mutableListOf<CapacityCycleFactor>(
            TieredCapacityCycleAdjuster(
                mutableListOf<Double>(2368.0, 4277.0, 5194.0, 8015.0),
                mutableListOf<Double>(1.06, 1.11, 0.82, 1.16),
            ),
            MinimumCapacityCycleAdjuster(36.4),
            CapacityCycleExpiresAtOffsetAdjuster(0.16),
            RoundingCapacityCycleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 72762.7,
    )
}
