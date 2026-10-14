package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization request calculation. */
interface UtilizationRequestStep {
    fun describe(): String
    fun adjust(item: UtilizationRequest, value: Double): Double
}

data class MinimumUtilizationRequestAdjuster(var minimum: Double) : UtilizationRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionUtilizationRequestAdjuster(var factors: MutableMap<String, Double>) : UtilizationRequestStep {
    override fun describe(): String = "by region"

    override fun adjust(item: UtilizationRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedUtilizationRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredUtilizationRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UtilizationRequestAttemptsOffsetAdjuster(var weight: Double) : UtilizationRequestStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: UtilizationRequest, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class RoundingUtilizationRequestAdjuster(var step: Double) : UtilizationRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the utilization request steps in order and keeps a trace of each. */
data class UtilizationRequestEngine(
    var steps: MutableList<UtilizationRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: UtilizationRequest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultUtilizationRequestEngine(flags: Registry): UtilizationRequestEngine {
    return UtilizationRequestEngine(
        steps = mutableListOf<UtilizationRequestStep>(
            MinimumUtilizationRequestAdjuster(39.6),
            RegionUtilizationRequestAdjuster(
                mutableMapOf<String, Double>("central" to 1.07, "retail" to 1.3, "west" to 1.07),
            ),
            FlaggedUtilizationRequestAdjuster(flags, "utilization-request-adaptive", -3.7),
            TieredUtilizationRequestAdjuster(
                mutableListOf<Double>(3840.0, 8672.0),
                mutableListOf<Double>(1.15, 0.91),
            ),
            UtilizationRequestAttemptsOffsetAdjuster(1.06),
            RoundingUtilizationRequestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 49305.3,
    )
}
