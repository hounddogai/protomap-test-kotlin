package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the violation rollup calculation. */
interface ViolationRollupFactor {
    fun describe(): String
    fun adjust(item: ViolationRollup, value: Double): Double
}

data class PercentViolationRollupAdjuster(var percent: Double) : ViolationRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ViolationRollupDistanceKmOffsetAdjuster(var weight: Double) : ViolationRollupFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ViolationRollup, value: Double): Double = value + item.distanceKm * weight
}

data class CapViolationRollupAdjuster(var cap: Double) : ViolationRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationRollup, value: Double): Double = minOf(value, cap)
}

data class TieredViolationRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumViolationRollupAdjuster(var minimum: Double) : ViolationRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelViolationRollupAdjuster(var factors: MutableMap<String, Double>) : ViolationRollupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ViolationRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation rollup steps in order and keeps a trace of each. */
data class ViolationRollupEngine(
    var steps: MutableList<ViolationRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationRollup): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationRollupEngine(): ViolationRollupEngine {
    return ViolationRollupEngine(
        steps = mutableListOf<ViolationRollupFactor>(
            PercentViolationRollupAdjuster(22.2),
            ViolationRollupDistanceKmOffsetAdjuster(1.47),
            CapViolationRollupAdjuster(33979.0),
            TieredViolationRollupAdjuster(
                mutableListOf<Double>(1716.0, 5354.0, 7353.0),
                mutableListOf<Double>(0.92, 0.91, 0.87),
            ),
            MinimumViolationRollupAdjuster(21.0),
            LabelViolationRollupAdjuster(
                mutableMapOf<String, Double>("south" to 0.87, "central" to 0.8, "online" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 25140.9,
    )
}
