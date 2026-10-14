package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the manual rate card adjustment calculation. */
interface ManualRateCardAdjustmentModifier {
    fun describe(): String
    fun adjust(item: ManualRateCardAdjustment, value: Double): Double
}

data class FlaggedManualRateCardAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRateCardAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRateCardAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualRateCardAdjustmentDiscountOffsetAdjuster(
    var weight: Double,
) : ManualRateCardAdjustmentModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: ManualRateCardAdjustment, value: Double): Double = value + item.discount * weight
}

data class MinimumManualRateCardAdjustmentAdjuster(var minimum: Double) : ManualRateCardAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRateCardAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualRateCardAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRateCardAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRateCardAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleManualRateCardAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRateCardAdjustmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualRateCardAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual rate card adjustment steps in order and keeps a trace of each. */
data class ManualRateCardAdjustmentEngine(
    var steps: MutableList<ManualRateCardAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRateCardAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualRateCardAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualRateCardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRateCardAdjustmentEngine(flags: Registry): ManualRateCardAdjustmentEngine {
    return ManualRateCardAdjustmentEngine(
        steps = mutableListOf<ManualRateCardAdjustmentModifier>(
            FlaggedManualRateCardAdjustmentAdjuster(flags, "manual-rate-card-adjustment-tiered", 5.9),
            ManualRateCardAdjustmentDiscountOffsetAdjuster(1.58),
            MinimumManualRateCardAdjustmentAdjuster(19.1),
            TieredManualRateCardAdjustmentAdjuster(
                mutableListOf<Double>(4450.0, 6365.0, 7459.0),
                mutableListOf<Double>(0.87, 1.05, 0.81),
            ),
            TitleManualRateCardAdjustmentAdjuster(
                mutableMapOf<String, Double>("north" to 0.78, "central" to 0.7, "online" to 1.27),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 92584.2,
    )
}
