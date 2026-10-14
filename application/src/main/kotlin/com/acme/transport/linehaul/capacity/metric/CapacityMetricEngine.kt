package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity metric calculation. */
interface CapacityMetricAdjuster {
    fun describe(): String
    fun adjust(item: CapacityMetric, value: Double): Double
}

data class CapCapacityMetricAdjuster(var cap: Double) : CapacityMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityMetric, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCapacityMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacityMetricStartsAtOffsetAdjuster(var weight: Double) : CapacityMetricAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CapacityMetric, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class LaneCapacityMetricAdjuster(var factors: MutableMap<String, Double>) : CapacityMetricAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: CapacityMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCapacityMetricAdjuster(var step: Double) : CapacityMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity metric steps in order and keeps a trace of each. */
data class CapacityMetricEngine(
    var steps: MutableList<CapacityMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityMetric): Double {
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

    fun evaluateAll(items: MutableList<CapacityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityMetricEngine(flags: Registry): CapacityMetricEngine {
    return CapacityMetricEngine(
        steps = mutableListOf<CapacityMetricAdjuster>(
            CapCapacityMetricAdjuster(40516.0),
            TieredCapacityMetricAdjuster(
                mutableListOf<Double>(4490.0, 6166.0),
                mutableListOf<Double>(0.93, 0.98),
            ),
            FlaggedCapacityMetricAdjuster(flags, "capacity-metric-global", -7.6),
            CapacityMetricStartsAtOffsetAdjuster(1.38),
            LaneCapacityMetricAdjuster(mutableMapOf<String, Double>("west" to 0.85, "east" to 1.02)),
            RoundingCapacityMetricAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 96701.2,
    )
}
