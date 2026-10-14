package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge override calculation. */
interface FuelSurchargeOverrideFactor {
    fun describe(): String
    fun adjust(item: FuelSurchargeOverride, value: Double): Double
}

data class SegmentFuelSurchargeOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFuelSurchargeOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFuelSurchargeOverrideAdjuster(var step: Double) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double = roundTo(value, step)
}

data class FuelSurchargeOverrideStartsAtOffsetAdjuster(var weight: Double) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class MinimumFuelSurchargeOverrideAdjuster(var minimum: Double) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelSurchargeOverrideAdjuster(var percent: Double) : FuelSurchargeOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel surcharge override steps in order and keeps a trace of each. */
data class FuelSurchargeOverrideEngine(
    var steps: MutableList<FuelSurchargeOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargeOverride): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargeOverrideEngine(flags: Registry): FuelSurchargeOverrideEngine {
    return FuelSurchargeOverrideEngine(
        steps = mutableListOf<FuelSurchargeOverrideFactor>(
            SegmentFuelSurchargeOverrideAdjuster(mutableMapOf<String, Double>("east" to 0.83, "south" to 1.32)),
            FlaggedFuelSurchargeOverrideAdjuster(flags, "fuel-surcharge-override-adaptive", -0.4),
            RoundingFuelSurchargeOverrideAdjuster(0.25),
            FuelSurchargeOverrideStartsAtOffsetAdjuster(1.54),
            MinimumFuelSurchargeOverrideAdjuster(22.0),
            PercentFuelSurchargeOverrideAdjuster(-21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 38001.9,
    )
}
