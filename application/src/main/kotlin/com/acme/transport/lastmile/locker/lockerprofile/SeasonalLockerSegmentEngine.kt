package com.acme.transport.lastmile.locker.lockerprofile

/** One step of the seasonal locker segment calculation. */
interface SeasonalLockerSegmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalLockerSegment, value: Double): Double
}

data class TitleSeasonalLockerSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalLockerSegmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalLockerSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalLockerSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalLockerSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalLockerSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalLockerSegmentAdjuster(var cap: Double) : SeasonalLockerSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalLockerSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalLockerSegmentAdjuster(var minimum: Double) : SeasonalLockerSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalLockerSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal locker segment steps in order and keeps a trace of each. */
data class SeasonalLockerSegmentEngine(
    var steps: MutableList<SeasonalLockerSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalLockerSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalLockerSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalLockerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalLockerSegmentEngine(): SeasonalLockerSegmentEngine {
    return SeasonalLockerSegmentEngine(
        steps = mutableListOf<SeasonalLockerSegmentFactor>(
            TitleSeasonalLockerSegmentAdjuster(mutableMapOf<String, Double>("central" to 0.91, "west" to 0.92)),
            TieredSeasonalLockerSegmentAdjuster(
                mutableListOf<Double>(85.0, 1755.0, 3837.0, 9898.0),
                mutableListOf<Double>(1.14, 1.14, 1.11, 1.04),
            ),
            CapSeasonalLockerSegmentAdjuster(10853.0),
            MinimumSeasonalLockerSegmentAdjuster(40.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 95556.5,
    )
}
