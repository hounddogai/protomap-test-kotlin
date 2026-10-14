package com.acme.transport.customs.tariff.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal tariff override calculation. */
interface SeasonalTariffOverrideFactor {
    fun describe(): String
    fun adjust(item: SeasonalTariffOverride, value: Double): Double
}

data class MinimumSeasonalTariffOverrideAdjuster(var minimum: Double) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameSeasonalTariffOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalTariffOverrideAdjuster(var percent: Double) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalTariffOverrideAdjuster(var step: Double) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double = roundTo(value, step)
}

data class SeasonalTariffOverrideRevisionOffsetAdjuster(var weight: Double) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class CapSeasonalTariffOverrideAdjuster(var cap: Double) : SeasonalTariffOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTariffOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal tariff override steps in order and keeps a trace of each. */
data class SeasonalTariffOverrideEngine(
    var steps: MutableList<SeasonalTariffOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTariffOverride): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTariffOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTariffOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTariffOverrideEngine(): SeasonalTariffOverrideEngine {
    return SeasonalTariffOverrideEngine(
        steps = mutableListOf<SeasonalTariffOverrideFactor>(
            MinimumSeasonalTariffOverrideAdjuster(14.0),
            NameSeasonalTariffOverrideAdjuster(mutableMapOf<String, Double>("west" to 1.15, "retail" to 1.15)),
            PercentSeasonalTariffOverrideAdjuster(-21.4),
            RoundingSeasonalTariffOverrideAdjuster(0.25),
            SeasonalTariffOverrideRevisionOffsetAdjuster(0.3),
            CapSeasonalTariffOverrideAdjuster(40433.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 19700.2,
    )
}
