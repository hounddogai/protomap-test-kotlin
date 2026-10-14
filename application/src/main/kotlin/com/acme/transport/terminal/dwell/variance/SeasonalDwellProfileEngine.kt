package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal dwell profile calculation. */
interface SeasonalDwellProfileModifier {
    fun describe(): String
    fun adjust(item: SeasonalDwellProfile, value: Double): Double
}

data class PercentSeasonalDwellProfileAdjuster(var percent: Double) : SeasonalDwellProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalDwellProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDwellProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalDwellProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDwellProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentSeasonalDwellProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDwellProfileModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalDwellProfileAdjuster(var step: Double) : SeasonalDwellProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalDwellProfileAdjuster(var minimum: Double) : SeasonalDwellProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDwellProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal dwell profile steps in order and keeps a trace of each. */
data class SeasonalDwellProfileEngine(
    var steps: MutableList<SeasonalDwellProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDwellProfile): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDwellProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDwellProfileEngine(flags: Registry): SeasonalDwellProfileEngine {
    return SeasonalDwellProfileEngine(
        steps = mutableListOf<SeasonalDwellProfileModifier>(
            PercentSeasonalDwellProfileAdjuster(18.8),
            TieredSeasonalDwellProfileAdjuster(
                mutableListOf<Double>(1971.0, 5725.0),
                mutableListOf<Double>(0.96, 0.95),
            ),
            FlaggedSeasonalDwellProfileAdjuster(flags, "seasonal-dwell-profile-scheduled", -8.0),
            SegmentSeasonalDwellProfileAdjuster(
                mutableMapOf<String, Double>("retail" to 1.09, "central" to 0.77),
            ),
            RoundingSeasonalDwellProfileAdjuster(0.5),
            MinimumSeasonalDwellProfileAdjuster(0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 19381.4,
    )
}
