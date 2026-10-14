package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal tender exception calculation. */
interface SeasonalTenderExceptionStep {
    fun describe(): String
    fun adjust(item: SeasonalTenderException, value: Double): Double
}

data class FlaggedSeasonalTenderExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTenderExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTenderException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalTenderExceptionAdjuster(var minimum: Double) : SeasonalTenderExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTenderException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalTenderExceptionMinimumOffsetAdjuster(var weight: Double) : SeasonalTenderExceptionStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: SeasonalTenderException, value: Double): Double = value + item.minimum * weight
}

data class PercentSeasonalTenderExceptionAdjuster(var percent: Double) : SeasonalTenderExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTenderException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalTenderExceptionAdjuster(var step: Double) : SeasonalTenderExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTenderException, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalTenderExceptionAdjuster(var cap: Double) : SeasonalTenderExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTenderException, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal tender exception steps in order and keeps a trace of each. */
data class SeasonalTenderExceptionEngine(
    var steps: MutableList<SeasonalTenderExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTenderException): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTenderExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTenderException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTenderExceptionEngine(flags: Registry): SeasonalTenderExceptionEngine {
    return SeasonalTenderExceptionEngine(
        steps = mutableListOf<SeasonalTenderExceptionStep>(
            FlaggedSeasonalTenderExceptionAdjuster(flags, "seasonal-tender-exception-manual", 0.2),
            MinimumSeasonalTenderExceptionAdjuster(47.3),
            SeasonalTenderExceptionMinimumOffsetAdjuster(1.94),
            PercentSeasonalTenderExceptionAdjuster(-12.6),
            RoundingSeasonalTenderExceptionAdjuster(0.1),
            CapSeasonalTenderExceptionAdjuster(25811.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 3764.3,
    )
}
