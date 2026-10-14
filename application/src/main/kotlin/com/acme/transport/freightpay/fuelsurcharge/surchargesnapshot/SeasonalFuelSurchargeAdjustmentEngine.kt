package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal fuel surcharge adjustment calculation. */
interface SeasonalFuelSurchargeAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double
}

data class PercentSeasonalFuelSurchargeAdjustmentAdjuster(
    var percent: Double,
) : SeasonalFuelSurchargeAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalFuelSurchargeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalFuelSurchargeAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalFuelSurchargeAdjustmentAdjuster(
    var step: Double,
) : SeasonalFuelSurchargeAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalFuelSurchargeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFuelSurchargeAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleSeasonalFuelSurchargeAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalFuelSurchargeAdjustmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalFuelSurchargeAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal fuel surcharge adjustment steps in order and keeps a trace of each. */
data class SeasonalFuelSurchargeAdjustmentEngine(
    var steps: MutableList<SeasonalFuelSurchargeAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalFuelSurchargeAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFuelSurchargeAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalFuelSurchargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalFuelSurchargeAdjustmentEngine(flags: Registry): SeasonalFuelSurchargeAdjustmentEngine {
    return SeasonalFuelSurchargeAdjustmentEngine(
        steps = mutableListOf<SeasonalFuelSurchargeAdjustmentAdjuster>(
            PercentSeasonalFuelSurchargeAdjustmentAdjuster(7.1),
            TieredSeasonalFuelSurchargeAdjustmentAdjuster(
                mutableListOf<Double>(78.0, 7152.0),
                mutableListOf<Double>(1.14, 0.81),
            ),
            RoundingSeasonalFuelSurchargeAdjustmentAdjuster(0.5),
            FlaggedSeasonalFuelSurchargeAdjustmentAdjuster(
                flags,
                "seasonal-fuel-surcharge-adjustment-manual",
                8.8,
            ),
            TitleSeasonalFuelSurchargeAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 0.73, "south" to 1.01, "west" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 42591.6,
    )
}
