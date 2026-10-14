package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary fuel burn metric calculation. */
interface PrimaryFuelBurnMetricFactor {
    fun describe(): String
    fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double
}

data class PrimaryFuelBurnMetricRatioOffsetAdjuster(var weight: Double) : PrimaryFuelBurnMetricFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double = value + item.ratio * weight
}

data class PercentPrimaryFuelBurnMetricAdjuster(var percent: Double) : PrimaryFuelBurnMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryFuelBurnMetricAdjuster(var step: Double) : PrimaryFuelBurnMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryFuelBurnMetricAdjuster(var cap: Double) : PrimaryFuelBurnMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryFuelBurnMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryFuelBurnMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryFuelBurnMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary fuel burn metric steps in order and keeps a trace of each. */
data class PrimaryFuelBurnMetricEngine(
    var steps: MutableList<PrimaryFuelBurnMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryFuelBurnMetric): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryFuelBurnMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryFuelBurnMetricEngine(flags: Registry): PrimaryFuelBurnMetricEngine {
    return PrimaryFuelBurnMetricEngine(
        steps = mutableListOf<PrimaryFuelBurnMetricFactor>(
            PrimaryFuelBurnMetricRatioOffsetAdjuster(1.96),
            PercentPrimaryFuelBurnMetricAdjuster(13.6),
            RoundingPrimaryFuelBurnMetricAdjuster(1.0),
            CapPrimaryFuelBurnMetricAdjuster(46908.0),
            FlaggedPrimaryFuelBurnMetricAdjuster(flags, "primary-fuel-burn-metric-weighted", -4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 90051.7,
    )
}
