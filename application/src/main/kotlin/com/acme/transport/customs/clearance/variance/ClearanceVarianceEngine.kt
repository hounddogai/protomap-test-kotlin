package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance variance calculation. */
interface ClearanceVarianceAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceVariance, value: Double): Double
}

data class FlaggedClearanceVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleClearanceVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ClearanceVarianceAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: ClearanceVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingClearanceVarianceAdjuster(var step: Double) : ClearanceVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceVariance, value: Double): Double = roundTo(value, step)
}

data class MinimumClearanceVarianceAdjuster(var minimum: Double) : ClearanceVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentClearanceVarianceAdjuster(var percent: Double) : ClearanceVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredClearanceVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance variance steps in order and keeps a trace of each. */
data class ClearanceVarianceEngine(
    var steps: MutableList<ClearanceVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearanceVariance): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceVarianceAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearanceVarianceEngine(flags: Registry): ClearanceVarianceEngine {
    return ClearanceVarianceEngine(
        steps = mutableListOf<ClearanceVarianceAdjuster>(
            FlaggedClearanceVarianceAdjuster(flags, "clearance-variance-seasonal", -0.8),
            LocaleClearanceVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 1.39, "north" to 1.25, "central" to 1.23),
            ),
            RoundingClearanceVarianceAdjuster(1.0),
            MinimumClearanceVarianceAdjuster(38.6),
            PercentClearanceVarianceAdjuster(-6.3),
            TieredClearanceVarianceAdjuster(
                mutableListOf<Double>(5577.0, 6552.0),
                mutableListOf<Double>(1.02, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 16812.1,
    )
}
