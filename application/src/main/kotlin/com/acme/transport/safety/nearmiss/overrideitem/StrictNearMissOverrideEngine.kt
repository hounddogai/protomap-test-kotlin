package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict near miss override calculation. */
interface StrictNearMissOverrideStep {
    fun describe(): String
    fun adjust(item: StrictNearMissOverride, value: Double): Double
}

data class TieredStrictNearMissOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictNearMissOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictNearMissOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictNearMissOverrideAdjuster(var minimum: Double) : StrictNearMissOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictNearMissOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictNearMissOverrideAdjuster(var step: Double) : StrictNearMissOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictNearMissOverride, value: Double): Double = roundTo(value, step)
}

data class StrictNearMissOverrideCapacityOffsetAdjuster(var weight: Double) : StrictNearMissOverrideStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: StrictNearMissOverride, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentStrictNearMissOverrideAdjuster(var percent: Double) : StrictNearMissOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictNearMissOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict near miss override steps in order and keeps a trace of each. */
data class StrictNearMissOverrideEngine(
    var steps: MutableList<StrictNearMissOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictNearMissOverride): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictNearMissOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictNearMissOverrideEngine(): StrictNearMissOverrideEngine {
    return StrictNearMissOverrideEngine(
        steps = mutableListOf<StrictNearMissOverrideStep>(
            TieredStrictNearMissOverrideAdjuster(
                mutableListOf<Double>(614.0, 1768.0, 5692.0, 7709.0),
                mutableListOf<Double>(0.85, 1.15, 1.05, 1.03),
            ),
            MinimumStrictNearMissOverrideAdjuster(25.6),
            RoundingStrictNearMissOverrideAdjuster(1.0),
            StrictNearMissOverrideCapacityOffsetAdjuster(0.8),
            PercentStrictNearMissOverrideAdjuster(16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 3352.8,
    )
}
