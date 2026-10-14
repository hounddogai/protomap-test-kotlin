package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal utilization target calculation. */
interface SeasonalUtilizationTargetStep {
    fun describe(): String
    fun adjust(item: SeasonalUtilizationTarget, value: Double): Double
}

data class SeasonalUtilizationTargetRevisionOffsetAdjuster(var weight: Double) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class FlaggedSeasonalUtilizationTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleSeasonalUtilizationTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalUtilizationTargetAdjuster(var step: Double) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalUtilizationTargetAdjuster(var percent: Double) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalUtilizationTargetAdjuster(var minimum: Double) : SeasonalUtilizationTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalUtilizationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal utilization target steps in order and keeps a trace of each. */
data class SeasonalUtilizationTargetEngine(
    var steps: MutableList<SeasonalUtilizationTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalUtilizationTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalUtilizationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalUtilizationTargetEngine(flags: Registry): SeasonalUtilizationTargetEngine {
    return SeasonalUtilizationTargetEngine(
        steps = mutableListOf<SeasonalUtilizationTargetStep>(
            SeasonalUtilizationTargetRevisionOffsetAdjuster(1.55),
            FlaggedSeasonalUtilizationTargetAdjuster(flags, "seasonal-utilization-target-default", -1.9),
            LocaleSeasonalUtilizationTargetAdjuster(
                mutableMapOf<String, Double>("west" to 1.39, "east" to 0.79, "retail" to 0.82),
            ),
            RoundingSeasonalUtilizationTargetAdjuster(0.5),
            PercentSeasonalUtilizationTargetAdjuster(-21.7),
            MinimumSeasonalUtilizationTargetAdjuster(20.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 1561.0,
    )
}
