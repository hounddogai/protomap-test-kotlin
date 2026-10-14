package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal fuel surcharge slot calculation. */
interface SeasonalFuelSurchargeSlotStep {
    fun describe(): String
    fun adjust(item: SeasonalFuelSurchargeSlot, value: Double): Double
}

data class TitleSeasonalFuelSurchargeSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalFuelSurchargeSlotStep {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalFuelSurchargeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalFuelSurchargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFuelSurchargeSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFuelSurchargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalFuelSurchargeSlotAdjuster(var minimum: Double) : SeasonalFuelSurchargeSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalFuelSurchargeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalFuelSurchargeSlotAdjuster(var step: Double) : SeasonalFuelSurchargeSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalFuelSurchargeSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal fuel surcharge slot steps in order and keeps a trace of each. */
data class SeasonalFuelSurchargeSlotEngine(
    var steps: MutableList<SeasonalFuelSurchargeSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalFuelSurchargeSlot): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFuelSurchargeSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalFuelSurchargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalFuelSurchargeSlotEngine(flags: Registry): SeasonalFuelSurchargeSlotEngine {
    return SeasonalFuelSurchargeSlotEngine(
        steps = mutableListOf<SeasonalFuelSurchargeSlotStep>(
            TitleSeasonalFuelSurchargeSlotAdjuster(
                mutableMapOf<String, Double>("south" to 1.04, "online" to 0.87),
            ),
            FlaggedSeasonalFuelSurchargeSlotAdjuster(flags, "seasonal-fuel-surcharge-slot-seasonal", 5.1),
            MinimumSeasonalFuelSurchargeSlotAdjuster(20.1),
            RoundingSeasonalFuelSurchargeSlotAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 76350.6,
    )
}
