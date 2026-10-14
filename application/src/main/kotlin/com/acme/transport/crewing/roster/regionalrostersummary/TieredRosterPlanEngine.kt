package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered roster plan calculation. */
interface TieredRosterPlanModifier {
    fun describe(): String
    fun adjust(item: TieredRosterPlan, value: Double): Double
}

data class PercentTieredRosterPlanAdjuster(var percent: Double) : TieredRosterPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRosterPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredRosterPlanAdjuster(var step: Double) : TieredRosterPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRosterPlan, value: Double): Double = roundTo(value, step)
}

data class TieredTieredRosterPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRosterPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRosterPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredRosterPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRosterPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRosterPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered roster plan steps in order and keeps a trace of each. */
data class TieredRosterPlanEngine(
    var steps: MutableList<TieredRosterPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredRosterPlan): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRosterPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredRosterPlanEngine(flags: Registry): TieredRosterPlanEngine {
    return TieredRosterPlanEngine(
        steps = mutableListOf<TieredRosterPlanModifier>(
            PercentTieredRosterPlanAdjuster(2.5),
            RoundingTieredRosterPlanAdjuster(5.0),
            TieredTieredRosterPlanAdjuster(
                mutableListOf<Double>(616.0, 8177.0, 8386.0),
                mutableListOf<Double>(0.81, 1.14, 0.84),
            ),
            FlaggedTieredRosterPlanAdjuster(flags, "tiered-roster-plan-rolling", -0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 25955.3,
    )
}
