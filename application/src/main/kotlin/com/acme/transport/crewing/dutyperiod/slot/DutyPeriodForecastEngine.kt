package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the duty period forecast calculation. */
interface DutyPeriodForecastModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodForecast, value: Double): Double
}

data class PercentDutyPeriodForecastAdjuster(var percent: Double) : DutyPeriodForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDutyPeriodForecastAdjuster(var cap: Double) : DutyPeriodForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double = minOf(value, cap)
}

data class TieredDutyPeriodForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DutyPeriodForecastLimitOffsetAdjuster(var weight: Double) : DutyPeriodForecastModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double = value + item.limit.toDouble() * weight
}

data class RoundingDutyPeriodForecastAdjuster(var step: Double) : DutyPeriodForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedDutyPeriodForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the duty period forecast steps in order and keeps a trace of each. */
data class DutyPeriodForecastEngine(
    var steps: MutableList<DutyPeriodForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodForecast): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodForecastEngine(flags: Registry): DutyPeriodForecastEngine {
    return DutyPeriodForecastEngine(
        steps = mutableListOf<DutyPeriodForecastModifier>(
            PercentDutyPeriodForecastAdjuster(12.3),
            CapDutyPeriodForecastAdjuster(18392.0),
            TieredDutyPeriodForecastAdjuster(
                mutableListOf<Double>(3008.0, 3808.0),
                mutableListOf<Double>(0.92, 1.06),
            ),
            DutyPeriodForecastLimitOffsetAdjuster(0.47),
            RoundingDutyPeriodForecastAdjuster(1.0),
            FlaggedDutyPeriodForecastAdjuster(flags, "duty-period-forecast-strict", 1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 71841.4,
    )
}
