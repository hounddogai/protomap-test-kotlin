package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal locker rule calculation. */
interface SeasonalLockerRuleStep {
    fun describe(): String
    fun adjust(item: SeasonalLockerRule, value: Double): Double
}

data class SeasonalLockerRuleVersionOffsetAdjuster(var weight: Double) : SeasonalLockerRuleStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: SeasonalLockerRule, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapSeasonalLockerRuleAdjuster(var cap: Double) : SeasonalLockerRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalLockerRule, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalLockerRuleAdjuster(var step: Double) : SeasonalLockerRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalLockerRule, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalLockerRuleAdjuster(var minimum: Double) : SeasonalLockerRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalLockerRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal locker rule steps in order and keeps a trace of each. */
data class SeasonalLockerRuleEngine(
    var steps: MutableList<SeasonalLockerRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalLockerRule): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalLockerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalLockerRuleEngine(): SeasonalLockerRuleEngine {
    return SeasonalLockerRuleEngine(
        steps = mutableListOf<SeasonalLockerRuleStep>(
            SeasonalLockerRuleVersionOffsetAdjuster(0.96),
            CapSeasonalLockerRuleAdjuster(811.0),
            RoundingSeasonalLockerRuleAdjuster(0.5),
            MinimumSeasonalLockerRuleAdjuster(42.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 57048.4,
    )
}
