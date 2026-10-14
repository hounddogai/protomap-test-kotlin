package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal redelivery estimate calculation. */
interface SeasonalRedeliveryEstimateModifier {
    fun describe(): String
    fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double
}

data class SeasonalRedeliveryEstimateQuantityOffsetAdjuster(
    var weight: Double,
) : SeasonalRedeliveryEstimateModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class PercentSeasonalRedeliveryEstimateAdjuster(var percent: Double) : SeasonalRedeliveryEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalRedeliveryEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRedeliveryEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalRedeliveryEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRedeliveryEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalRedeliveryEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRedeliveryEstimateModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalRedeliveryEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal redelivery estimate steps in order and keeps a trace of each. */
data class SeasonalRedeliveryEstimateEngine(
    var steps: MutableList<SeasonalRedeliveryEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalRedeliveryEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRedeliveryEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalRedeliveryEstimateEngine(flags: Registry): SeasonalRedeliveryEstimateEngine {
    return SeasonalRedeliveryEstimateEngine(
        steps = mutableListOf<SeasonalRedeliveryEstimateModifier>(
            SeasonalRedeliveryEstimateQuantityOffsetAdjuster(1.73),
            PercentSeasonalRedeliveryEstimateAdjuster(12.4),
            FlaggedSeasonalRedeliveryEstimateAdjuster(flags, "seasonal-redelivery-estimate-legacy", 0.4),
            TieredSeasonalRedeliveryEstimateAdjuster(
                mutableListOf<Double>(2232.0, 4866.0, 8702.0, 9017.0),
                mutableListOf<Double>(1.02, 1.1, 0.84, 0.95),
            ),
            LabelSeasonalRedeliveryEstimateAdjuster(mutableMapOf<String, Double>("north" to 0.79)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 28348.3,
    )
}
