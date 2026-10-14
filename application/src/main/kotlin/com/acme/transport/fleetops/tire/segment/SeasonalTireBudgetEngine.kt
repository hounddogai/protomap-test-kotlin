package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal tire budget calculation. */
interface SeasonalTireBudgetStep {
    fun describe(): String
    fun adjust(item: SeasonalTireBudget, value: Double): Double
}

data class TieredSeasonalTireBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTireBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalTireBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTireBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionSeasonalTireBudgetAdjuster(var factors: MutableMap<String, Double>) : SeasonalTireBudgetStep {
    override fun describe(): String = "by region"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalTireBudgetAdjuster(var cap: Double) : SeasonalTireBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalTireBudgetAdjuster(var minimum: Double) : SeasonalTireBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalTireBudgetEtaMinutesOffsetAdjuster(var weight: Double) : SeasonalTireBudgetStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: SeasonalTireBudget, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the seasonal tire budget steps in order and keeps a trace of each. */
data class SeasonalTireBudgetEngine(
    var steps: MutableList<SeasonalTireBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTireBudget): Double {
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

    fun evaluateAll(items: MutableList<SeasonalTireBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTireBudgetEngine(flags: Registry): SeasonalTireBudgetEngine {
    return SeasonalTireBudgetEngine(
        steps = mutableListOf<SeasonalTireBudgetStep>(
            TieredSeasonalTireBudgetAdjuster(
                mutableListOf<Double>(715.0, 6975.0, 8909.0, 9395.0),
                mutableListOf<Double>(1.2, 1.16, 0.81, 0.92),
            ),
            FlaggedSeasonalTireBudgetAdjuster(flags, "seasonal-tire-budget-pending", -8.0),
            RegionSeasonalTireBudgetAdjuster(
                mutableMapOf<String, Double>("east" to 1.22, "central" to 0.93, "online" to 1.07),
            ),
            CapSeasonalTireBudgetAdjuster(34213.0),
            MinimumSeasonalTireBudgetAdjuster(18.3),
            SeasonalTireBudgetEtaMinutesOffsetAdjuster(0.94),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 33240.6,
    )
}
