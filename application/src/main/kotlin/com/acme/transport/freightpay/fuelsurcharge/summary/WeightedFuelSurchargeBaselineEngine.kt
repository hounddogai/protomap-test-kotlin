package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted fuel surcharge baseline calculation. */
interface WeightedFuelSurchargeBaselineStep {
    fun describe(): String
    fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double
}

data class PercentWeightedFuelSurchargeBaselineAdjuster(
    var percent: Double,
) : WeightedFuelSurchargeBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedFuelSurchargeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelSurchargeBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedFuelSurchargeBaselineAdjuster(
    var minimum: Double,
) : WeightedFuelSurchargeBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotWeightedFuelSurchargeBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFuelSurchargeBaselineStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedFuelSurchargeBaselineDurationMinutesOffsetAdjuster(
    var weight: Double,
) : WeightedFuelSurchargeBaselineStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: WeightedFuelSurchargeBaseline, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the weighted fuel surcharge baseline steps in order and keeps a trace of each. */
data class WeightedFuelSurchargeBaselineEngine(
    var steps: MutableList<WeightedFuelSurchargeBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedFuelSurchargeBaseline): Double {
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

    fun evaluateAll(items: MutableList<WeightedFuelSurchargeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedFuelSurchargeBaselineEngine(flags: Registry): WeightedFuelSurchargeBaselineEngine {
    return WeightedFuelSurchargeBaselineEngine(
        steps = mutableListOf<WeightedFuelSurchargeBaselineStep>(
            PercentWeightedFuelSurchargeBaselineAdjuster(-23.2),
            FlaggedWeightedFuelSurchargeBaselineAdjuster(flags, "weighted-fuel-surcharge-baseline-pending", 5.6),
            MinimumWeightedFuelSurchargeBaselineAdjuster(9.8),
            DepotWeightedFuelSurchargeBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 0.84, "online" to 1.07, "south" to 1.15),
            ),
            WeightedFuelSurchargeBaselineDurationMinutesOffsetAdjuster(1.62),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 80832.9,
    )
}
