package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.flags.Registry

/** One step of the regional fuel burn score calculation. */
interface RegionalFuelBurnScoreFactor {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnScore, value: Double): Double
}

data class CapRegionalFuelBurnScoreAdjuster(var cap: Double) : RegionalFuelBurnScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnScore, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalFuelBurnScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelBurnScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelBurnScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitRegionalFuelBurnScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelBurnScoreFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalFuelBurnScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional fuel burn score steps in order and keeps a trace of each. */
data class RegionalFuelBurnScoreEngine(
    var steps: MutableList<RegionalFuelBurnScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalFuelBurnScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelBurnScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelBurnScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnScoreEngine(flags: Registry): RegionalFuelBurnScoreEngine {
    return RegionalFuelBurnScoreEngine(
        steps = mutableListOf<RegionalFuelBurnScoreFactor>(
            CapRegionalFuelBurnScoreAdjuster(44522.0),
            FlaggedRegionalFuelBurnScoreAdjuster(flags, "regional-fuel-burn-score-pending", 9.0),
            UnitRegionalFuelBurnScoreAdjuster(mutableMapOf<String, Double>("south" to 0.83)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 89245.5,
    )
}
