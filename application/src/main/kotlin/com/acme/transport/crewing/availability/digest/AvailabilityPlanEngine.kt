package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the availability plan calculation. */
interface AvailabilityPlanAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityPlan, value: Double): Double
}

data class PercentAvailabilityPlanAdjuster(var percent: Double) : AvailabilityPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAvailabilityPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAvailabilityPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelAvailabilityPlanAdjuster(var factors: MutableMap<String, Double>) : AvailabilityPlanAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AvailabilityPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the availability plan steps in order and keeps a trace of each. */
data class AvailabilityPlanEngine(
    var steps: MutableList<AvailabilityPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityPlan): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityPlanEngine(flags: Registry): AvailabilityPlanEngine {
    return AvailabilityPlanEngine(
        steps = mutableListOf<AvailabilityPlanAdjuster>(
            PercentAvailabilityPlanAdjuster(4.9),
            FlaggedAvailabilityPlanAdjuster(flags, "availability-plan-seasonal", 7.4),
            TieredAvailabilityPlanAdjuster(
                mutableListOf<Double>(119.0, 410.0, 647.0, 7554.0),
                mutableListOf<Double>(1.08, 1.01, 0.8, 0.97),
            ),
            LabelAvailabilityPlanAdjuster(mutableMapOf<String, Double>("east" to 0.88, "central" to 0.97)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 29581.0,
    )
}
