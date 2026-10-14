package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation baseline calculation. */
interface ViolationBaselineFactor {
    fun describe(): String
    fun adjust(item: ViolationBaseline, value: Double): Double
}

data class ViolationBaselineEtaMinutesOffsetAdjuster(var weight: Double) : ViolationBaselineFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ViolationBaseline, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class RoundingViolationBaselineAdjuster(var step: Double) : ViolationBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationBaseline, value: Double): Double = roundTo(value, step)
}

data class ChannelViolationBaselineAdjuster(var factors: MutableMap<String, Double>) : ViolationBaselineFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: ViolationBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumViolationBaselineAdjuster(var minimum: Double) : ViolationBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentViolationBaselineAdjuster(var percent: Double) : ViolationBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredViolationBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the violation baseline steps in order and keeps a trace of each. */
data class ViolationBaselineEngine(
    var steps: MutableList<ViolationBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationBaseline): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationBaselineEngine(): ViolationBaselineEngine {
    return ViolationBaselineEngine(
        steps = mutableListOf<ViolationBaselineFactor>(
            ViolationBaselineEtaMinutesOffsetAdjuster(1.52),
            RoundingViolationBaselineAdjuster(0.25),
            ChannelViolationBaselineAdjuster(
                mutableMapOf<String, Double>("north" to 1.06, "online" to 1.26, "east" to 0.87),
            ),
            MinimumViolationBaselineAdjuster(22.7),
            PercentViolationBaselineAdjuster(3.5),
            TieredViolationBaselineAdjuster(
                mutableListOf<Double>(5684.0, 9512.0),
                mutableListOf<Double>(0.92, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 63183.5,
    )
}
