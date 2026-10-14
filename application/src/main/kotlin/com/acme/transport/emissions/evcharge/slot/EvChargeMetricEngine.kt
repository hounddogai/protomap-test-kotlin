package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge metric calculation. */
interface EvChargeMetricAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeMetric, value: Double): Double
}

data class RoundingEvChargeMetricAdjuster(var step: Double) : EvChargeMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeMetric, value: Double): Double = roundTo(value, step)
}

data class ChannelEvChargeMetricAdjuster(var factors: MutableMap<String, Double>) : EvChargeMetricAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: EvChargeMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumEvChargeMetricAdjuster(var minimum: Double) : EvChargeMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEvChargeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedEvChargeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapEvChargeMetricAdjuster(var cap: Double) : EvChargeMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge metric steps in order and keeps a trace of each. */
data class EvChargeMetricEngine(
    var steps: MutableList<EvChargeMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeMetricEngine(flags: Registry): EvChargeMetricEngine {
    return EvChargeMetricEngine(
        steps = mutableListOf<EvChargeMetricAdjuster>(
            RoundingEvChargeMetricAdjuster(0.05),
            ChannelEvChargeMetricAdjuster(mutableMapOf<String, Double>("south" to 1.1)),
            MinimumEvChargeMetricAdjuster(30.0),
            TieredEvChargeMetricAdjuster(
                mutableListOf<Double>(2280.0, 8149.0),
                mutableListOf<Double>(1.11, 0.89),
            ),
            FlaggedEvChargeMetricAdjuster(flags, "ev-charge-metric-default", -5.5),
            CapEvChargeMetricAdjuster(27933.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 29373.0,
    )
}
