package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive dock door metric calculation. */
interface AdaptiveDockDoorMetricFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double
}

data class FlaggedAdaptiveDockDoorMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDockDoorMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveDockDoorMetricAdjuster(var cap: Double) : AdaptiveDockDoorMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveDockDoorMetricAdjuster(var step: Double) : AdaptiveDockDoorMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double = roundTo(value, step)
}

data class NameAdaptiveDockDoorMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDockDoorMetricFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveDockDoorMetricAdjuster(var minimum: Double) : AdaptiveDockDoorMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDockDoorMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive dock door metric steps in order and keeps a trace of each. */
data class AdaptiveDockDoorMetricEngine(
    var steps: MutableList<AdaptiveDockDoorMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveDockDoorMetric): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDockDoorMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveDockDoorMetricEngine(flags: Registry): AdaptiveDockDoorMetricEngine {
    return AdaptiveDockDoorMetricEngine(
        steps = mutableListOf<AdaptiveDockDoorMetricFactor>(
            FlaggedAdaptiveDockDoorMetricAdjuster(flags, "adaptive-dock-door-metric-regional", -6.2),
            CapAdaptiveDockDoorMetricAdjuster(994.0),
            RoundingAdaptiveDockDoorMetricAdjuster(0.1),
            NameAdaptiveDockDoorMetricAdjuster(mutableMapOf<String, Double>("online" to 1.2)),
            MinimumAdaptiveDockDoorMetricAdjuster(8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 73001.1,
    )
}
