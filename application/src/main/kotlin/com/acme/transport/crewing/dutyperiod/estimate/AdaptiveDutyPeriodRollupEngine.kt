package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive duty period rollup calculation. */
interface AdaptiveDutyPeriodRollupFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double
}

data class MinimumAdaptiveDutyPeriodRollupAdjuster(var minimum: Double) : AdaptiveDutyPeriodRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveDutyPeriodRollupAdjuster(var step: Double) : AdaptiveDutyPeriodRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveDutyPeriodRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDutyPeriodRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionAdaptiveDutyPeriodRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDutyPeriodRollupFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveDutyPeriodRollupAdjuster(var percent: Double) : AdaptiveDutyPeriodRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDutyPeriodRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive duty period rollup steps in order and keeps a trace of each. */
data class AdaptiveDutyPeriodRollupEngine(
    var steps: MutableList<AdaptiveDutyPeriodRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDutyPeriodRollup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDutyPeriodRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDutyPeriodRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDutyPeriodRollupEngine(flags: Registry): AdaptiveDutyPeriodRollupEngine {
    return AdaptiveDutyPeriodRollupEngine(
        steps = mutableListOf<AdaptiveDutyPeriodRollupFactor>(
            MinimumAdaptiveDutyPeriodRollupAdjuster(19.7),
            RoundingAdaptiveDutyPeriodRollupAdjuster(1.0),
            FlaggedAdaptiveDutyPeriodRollupAdjuster(flags, "adaptive-duty-period-rollup-weighted", -1.5),
            RegionAdaptiveDutyPeriodRollupAdjuster(mutableMapOf<String, Double>("north" to 0.82)),
            PercentAdaptiveDutyPeriodRollupAdjuster(-5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 55970.6,
    )
}
