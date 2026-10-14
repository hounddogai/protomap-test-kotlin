package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal idling window calculation. */
interface SeasonalIdlingWindowAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalIdlingWindow, value: Double): Double
}

data class SeasonalIdlingWindowAttemptsOffsetAdjuster(var weight: Double) : SeasonalIdlingWindowAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: SeasonalIdlingWindow, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedSeasonalIdlingWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalIdlingWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalIdlingWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleSeasonalIdlingWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalIdlingWindowAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalIdlingWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalIdlingWindowAdjuster(var cap: Double) : SeasonalIdlingWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalIdlingWindow, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalIdlingWindowAdjuster(var percent: Double) : SeasonalIdlingWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalIdlingWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal idling window steps in order and keeps a trace of each. */
data class SeasonalIdlingWindowEngine(
    var steps: MutableList<SeasonalIdlingWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalIdlingWindow): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalIdlingWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalIdlingWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalIdlingWindowEngine(flags: Registry): SeasonalIdlingWindowEngine {
    return SeasonalIdlingWindowEngine(
        steps = mutableListOf<SeasonalIdlingWindowAdjuster>(
            SeasonalIdlingWindowAttemptsOffsetAdjuster(0.18),
            FlaggedSeasonalIdlingWindowAdjuster(flags, "seasonal-idling-window-global", 4.0),
            LocaleSeasonalIdlingWindowAdjuster(
                mutableMapOf<String, Double>("west" to 0.92, "central" to 1.26, "north" to 0.87),
            ),
            CapSeasonalIdlingWindowAdjuster(28606.0),
            PercentSeasonalIdlingWindowAdjuster(11.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 63916.1,
    )
}
