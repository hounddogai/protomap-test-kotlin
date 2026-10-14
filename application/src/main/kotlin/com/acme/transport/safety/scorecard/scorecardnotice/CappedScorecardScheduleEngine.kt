package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped scorecard schedule calculation. */
interface CappedScorecardScheduleFactor {
    fun describe(): String
    fun adjust(item: CappedScorecardSchedule, value: Double): Double
}

data class CappedScorecardScheduleMaximumOffsetAdjuster(var weight: Double) : CappedScorecardScheduleFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: CappedScorecardSchedule, value: Double): Double = value + item.maximum * weight
}

data class ChannelCappedScorecardScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedScorecardScheduleFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: CappedScorecardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedScorecardScheduleAdjuster(var step: Double) : CappedScorecardScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedScorecardSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedScorecardScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedScorecardScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedScorecardSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedScorecardScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedScorecardScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedScorecardSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped scorecard schedule steps in order and keeps a trace of each. */
data class CappedScorecardScheduleEngine(
    var steps: MutableList<CappedScorecardScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedScorecardSchedule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedScorecardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedScorecardScheduleEngine(flags: Registry): CappedScorecardScheduleEngine {
    return CappedScorecardScheduleEngine(
        steps = mutableListOf<CappedScorecardScheduleFactor>(
            CappedScorecardScheduleMaximumOffsetAdjuster(1.03),
            ChannelCappedScorecardScheduleAdjuster(
                mutableMapOf<String, Double>("west" to 0.84, "north" to 0.88, "retail" to 1.16),
            ),
            RoundingCappedScorecardScheduleAdjuster(1.0),
            FlaggedCappedScorecardScheduleAdjuster(flags, "capped-scorecard-schedule-global", 3.1),
            TieredCappedScorecardScheduleAdjuster(
                mutableListOf<Double>(1865.0, 6224.0, 8179.0),
                mutableListOf<Double>(0.9, 1.06, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 89874.8,
    )
}
