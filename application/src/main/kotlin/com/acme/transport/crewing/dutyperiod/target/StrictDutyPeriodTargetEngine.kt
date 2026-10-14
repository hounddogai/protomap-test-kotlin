package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict duty period target calculation. */
interface StrictDutyPeriodTargetStep {
    fun describe(): String
    fun adjust(item: StrictDutyPeriodTarget, value: Double): Double
}

data class FlaggedStrictDutyPeriodTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDutyPeriodTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDutyPeriodTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictDutyPeriodTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDutyPeriodTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDutyPeriodTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentStrictDutyPeriodTargetAdjuster(var percent: Double) : StrictDutyPeriodTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDutyPeriodTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumStrictDutyPeriodTargetAdjuster(var minimum: Double) : StrictDutyPeriodTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDutyPeriodTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict duty period target steps in order and keeps a trace of each. */
data class StrictDutyPeriodTargetEngine(
    var steps: MutableList<StrictDutyPeriodTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDutyPeriodTarget): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictDutyPeriodTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDutyPeriodTargetEngine(flags: Registry): StrictDutyPeriodTargetEngine {
    return StrictDutyPeriodTargetEngine(
        steps = mutableListOf<StrictDutyPeriodTargetStep>(
            FlaggedStrictDutyPeriodTargetAdjuster(flags, "strict-duty-period-target-manual", -9.4),
            TieredStrictDutyPeriodTargetAdjuster(
                mutableListOf<Double>(5245.0, 7087.0),
                mutableListOf<Double>(0.97, 1.16),
            ),
            PercentStrictDutyPeriodTargetAdjuster(-10.6),
            MinimumStrictDutyPeriodTargetAdjuster(32.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 82567.0,
    )
}
