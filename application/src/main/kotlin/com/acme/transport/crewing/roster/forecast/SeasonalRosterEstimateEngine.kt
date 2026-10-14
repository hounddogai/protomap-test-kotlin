package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal roster estimate calculation. */
interface SeasonalRosterEstimateStep {
    fun describe(): String
    fun adjust(item: SeasonalRosterEstimate, value: Double): Double
}

data class SeasonalRosterEstimateLimitOffsetAdjuster(var weight: Double) : SeasonalRosterEstimateStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: SeasonalRosterEstimate, value: Double): Double = value + item.limit.toDouble() * weight
}

data class RoundingSeasonalRosterEstimateAdjuster(var step: Double) : SeasonalRosterEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRosterEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalRosterEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRosterEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRosterEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalRosterEstimateAdjuster(var percent: Double) : SeasonalRosterEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRosterEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneSeasonalRosterEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRosterEstimateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: SeasonalRosterEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal roster estimate steps in order and keeps a trace of each. */
data class SeasonalRosterEstimateEngine(
    var steps: MutableList<SeasonalRosterEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalRosterEstimate): Double {
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

    fun evaluateAll(items: MutableList<SeasonalRosterEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalRosterEstimateEngine(flags: Registry): SeasonalRosterEstimateEngine {
    return SeasonalRosterEstimateEngine(
        steps = mutableListOf<SeasonalRosterEstimateStep>(
            SeasonalRosterEstimateLimitOffsetAdjuster(1.93),
            RoundingSeasonalRosterEstimateAdjuster(0.5),
            FlaggedSeasonalRosterEstimateAdjuster(flags, "seasonal-roster-estimate-fallback", 0.2),
            PercentSeasonalRosterEstimateAdjuster(-23.2),
            LaneSeasonalRosterEstimateAdjuster(
                mutableMapOf<String, Double>("online" to 1.32, "west" to 0.81, "east" to 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 43620.3,
    )
}
