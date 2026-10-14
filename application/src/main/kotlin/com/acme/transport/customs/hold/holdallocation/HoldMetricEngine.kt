package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold metric calculation. */
interface HoldMetricModifier {
    fun describe(): String
    fun adjust(item: HoldMetric, value: Double): Double
}

data class PercentHoldMetricAdjuster(var percent: Double) : HoldMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredHoldMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class HoldMetricRetriesOffsetAdjuster(var weight: Double) : HoldMetricModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: HoldMetric, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedHoldMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumHoldMetricAdjuster(var minimum: Double) : HoldMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hold metric steps in order and keeps a trace of each. */
data class HoldMetricEngine(
    var steps: MutableList<HoldMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldMetric): Double {
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

    fun evaluateAll(items: MutableList<HoldMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldMetricEngine(flags: Registry): HoldMetricEngine {
    return HoldMetricEngine(
        steps = mutableListOf<HoldMetricModifier>(
            PercentHoldMetricAdjuster(-21.2),
            TieredHoldMetricAdjuster(mutableListOf<Double>(1202.0, 8423.0), mutableListOf<Double>(0.94, 1.16)),
            HoldMetricRetriesOffsetAdjuster(1.2),
            FlaggedHoldMetricAdjuster(flags, "hold-metric-capped", -0.4),
            MinimumHoldMetricAdjuster(18.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 97276.9,
    )
}
