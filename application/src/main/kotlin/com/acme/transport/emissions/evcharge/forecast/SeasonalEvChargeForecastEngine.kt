package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal ev charge forecast calculation. */
interface SeasonalEvChargeForecastFactor {
    fun describe(): String
    fun adjust(item: SeasonalEvChargeForecast, value: Double): Double
}

data class MinimumSeasonalEvChargeForecastAdjuster(var minimum: Double) : SeasonalEvChargeForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalEvChargeForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalEvChargeForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalEvChargeForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalEvChargeForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalEvChargeForecastAdjuster(var step: Double) : SeasonalEvChargeForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalEvChargeForecast, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalEvChargeForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalEvChargeForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalEvChargeForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalEvChargeForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalEvChargeForecastFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalEvChargeForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal ev charge forecast steps in order and keeps a trace of each. */
data class SeasonalEvChargeForecastEngine(
    var steps: MutableList<SeasonalEvChargeForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalEvChargeForecast): Double {
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

    fun evaluateAll(items: MutableList<SeasonalEvChargeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalEvChargeForecastEngine(flags: Registry): SeasonalEvChargeForecastEngine {
    return SeasonalEvChargeForecastEngine(
        steps = mutableListOf<SeasonalEvChargeForecastFactor>(
            MinimumSeasonalEvChargeForecastAdjuster(15.8),
            FlaggedSeasonalEvChargeForecastAdjuster(flags, "seasonal-ev-charge-forecast-lenient", 3.5),
            RoundingSeasonalEvChargeForecastAdjuster(1.0),
            TieredSeasonalEvChargeForecastAdjuster(
                mutableListOf<Double>(68.0, 4620.0, 9706.0),
                mutableListOf<Double>(1.19, 1.14, 0.99),
            ),
            LabelSeasonalEvChargeForecastAdjuster(
                mutableMapOf<String, Double>("north" to 1.22, "online" to 0.96, "east" to 1.37),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 62158.0,
    )
}
