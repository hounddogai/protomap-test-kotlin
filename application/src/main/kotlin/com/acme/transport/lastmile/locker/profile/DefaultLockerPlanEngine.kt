package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default locker plan calculation. */
interface DefaultLockerPlanStep {
    fun describe(): String
    fun adjust(item: DefaultLockerPlan, value: Double): Double
}

data class DefaultLockerPlanMaximumOffsetAdjuster(var weight: Double) : DefaultLockerPlanStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double = value + item.maximum * weight
}

data class CapDefaultLockerPlanAdjuster(var cap: Double) : DefaultLockerPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double = minOf(value, cap)
}

data class TitleDefaultLockerPlanAdjuster(var factors: MutableMap<String, Double>) : DefaultLockerPlanStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultLockerPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultLockerPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultLockerPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLockerPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultLockerPlanAdjuster(var percent: Double) : DefaultLockerPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default locker plan steps in order and keeps a trace of each. */
data class DefaultLockerPlanEngine(
    var steps: MutableList<DefaultLockerPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultLockerPlan): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLockerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultLockerPlanEngine(flags: Registry): DefaultLockerPlanEngine {
    return DefaultLockerPlanEngine(
        steps = mutableListOf<DefaultLockerPlanStep>(
            DefaultLockerPlanMaximumOffsetAdjuster(1.52),
            CapDefaultLockerPlanAdjuster(6042.0),
            TitleDefaultLockerPlanAdjuster(
                mutableMapOf<String, Double>("online" to 0.74, "retail" to 1.23, "east" to 1.12),
            ),
            TieredDefaultLockerPlanAdjuster(
                mutableListOf<Double>(13.0, 6490.0, 6779.0, 8928.0),
                mutableListOf<Double>(0.96, 0.98, 1.09, 1.18),
            ),
            FlaggedDefaultLockerPlanAdjuster(flags, "default-locker-plan-regional", 2.0),
            PercentDefaultLockerPlanAdjuster(12.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 96726.9,
    )
}
