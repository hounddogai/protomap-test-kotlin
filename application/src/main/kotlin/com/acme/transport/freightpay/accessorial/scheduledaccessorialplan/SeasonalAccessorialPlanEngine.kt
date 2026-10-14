package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal accessorial plan calculation. */
interface SeasonalAccessorialPlanStep {
    fun describe(): String
    fun adjust(item: SeasonalAccessorialPlan, value: Double): Double
}

data class RoundingSeasonalAccessorialPlanAdjuster(var step: Double) : SeasonalAccessorialPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAccessorialPlan, value: Double): Double = roundTo(value, step)
}

data class SeasonalAccessorialPlanLimitOffsetAdjuster(var weight: Double) : SeasonalAccessorialPlanStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: SeasonalAccessorialPlan, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedSeasonalAccessorialPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAccessorialPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAccessorialPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalAccessorialPlanAdjuster(var cap: Double) : SeasonalAccessorialPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalAccessorialPlan, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalAccessorialPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAccessorialPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAccessorialPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal accessorial plan steps in order and keeps a trace of each. */
data class SeasonalAccessorialPlanEngine(
    var steps: MutableList<SeasonalAccessorialPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalAccessorialPlan): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAccessorialPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAccessorialPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalAccessorialPlanEngine(flags: Registry): SeasonalAccessorialPlanEngine {
    return SeasonalAccessorialPlanEngine(
        steps = mutableListOf<SeasonalAccessorialPlanStep>(
            RoundingSeasonalAccessorialPlanAdjuster(0.1),
            SeasonalAccessorialPlanLimitOffsetAdjuster(0.21),
            FlaggedSeasonalAccessorialPlanAdjuster(flags, "seasonal-accessorial-plan-regional", -9.5),
            CapSeasonalAccessorialPlanAdjuster(32893.0),
            TieredSeasonalAccessorialPlanAdjuster(
                mutableListOf<Double>(3622.0, 6250.0, 7871.0),
                mutableListOf<Double>(0.89, 1.04, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 32770.7,
    )
}
