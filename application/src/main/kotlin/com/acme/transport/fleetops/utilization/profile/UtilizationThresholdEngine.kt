package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization threshold calculation. */
interface UtilizationThresholdStep {
    fun describe(): String
    fun adjust(item: UtilizationThreshold, value: Double): Double
}

data class TieredUtilizationThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleUtilizationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationThresholdStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: UtilizationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingUtilizationThresholdAdjuster(var step: Double) : UtilizationThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedUtilizationThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the utilization threshold steps in order and keeps a trace of each. */
data class UtilizationThresholdEngine(
    var steps: MutableList<UtilizationThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationThresholdEngine(flags: Registry): UtilizationThresholdEngine {
    return UtilizationThresholdEngine(
        steps = mutableListOf<UtilizationThresholdStep>(
            TieredUtilizationThresholdAdjuster(
                mutableListOf<Double>(570.0, 2537.0, 6634.0),
                mutableListOf<Double>(0.86, 0.86, 1.1),
            ),
            LocaleUtilizationThresholdAdjuster(mutableMapOf<String, Double>("north" to 0.81, "south" to 0.72)),
            RoundingUtilizationThresholdAdjuster(5.0),
            FlaggedUtilizationThresholdAdjuster(flags, "utilization-threshold-capped", -4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 14429.5,
    )
}
