package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary backhaul metric calculation. */
interface PrimaryBackhaulMetricModifier {
    fun describe(): String
    fun adjust(item: PrimaryBackhaulMetric, value: Double): Double
}

data class FlaggedPrimaryBackhaulMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryBackhaulMetricAdjuster(var cap: Double) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryBackhaulMetricAdjuster(var minimum: Double) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryBackhaulMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryBackhaulMetricLimitOffsetAdjuster(var weight: Double) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double = value + item.limit.toDouble() * weight
}

data class LabelPrimaryBackhaulMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryBackhaulMetricModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryBackhaulMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary backhaul metric steps in order and keeps a trace of each. */
data class PrimaryBackhaulMetricEngine(
    var steps: MutableList<PrimaryBackhaulMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryBackhaulMetric): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryBackhaulMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryBackhaulMetricEngine(flags: Registry): PrimaryBackhaulMetricEngine {
    return PrimaryBackhaulMetricEngine(
        steps = mutableListOf<PrimaryBackhaulMetricModifier>(
            FlaggedPrimaryBackhaulMetricAdjuster(flags, "primary-backhaul-metric-batched", 6.8),
            CapPrimaryBackhaulMetricAdjuster(874.0),
            MinimumPrimaryBackhaulMetricAdjuster(30.7),
            TieredPrimaryBackhaulMetricAdjuster(
                mutableListOf<Double>(5083.0, 5428.0, 6900.0),
                mutableListOf<Double>(1.1, 1.07, 1.16),
            ),
            PrimaryBackhaulMetricLimitOffsetAdjuster(1.94),
            LabelPrimaryBackhaulMetricAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 22500.2,
    )
}
