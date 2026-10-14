package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary rest break notice calculation. */
interface PrimaryRestBreakNoticeAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryRestBreakNotice, value: Double): Double
}

data class RoundingPrimaryRestBreakNoticeAdjuster(var step: Double) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryRestBreakNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryRestBreakNoticeOdometerOffsetAdjuster(var weight: Double) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class PercentPrimaryRestBreakNoticeAdjuster(var percent: Double) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryRestBreakNoticeAdjuster(var minimum: Double) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NamePrimaryRestBreakNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRestBreakNoticeAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryRestBreakNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary rest break notice steps in order and keeps a trace of each. */
data class PrimaryRestBreakNoticeEngine(
    var steps: MutableList<PrimaryRestBreakNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryRestBreakNotice): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRestBreakNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRestBreakNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryRestBreakNoticeEngine(): PrimaryRestBreakNoticeEngine {
    return PrimaryRestBreakNoticeEngine(
        steps = mutableListOf<PrimaryRestBreakNoticeAdjuster>(
            RoundingPrimaryRestBreakNoticeAdjuster(0.25),
            TieredPrimaryRestBreakNoticeAdjuster(
                mutableListOf<Double>(433.0, 6758.0, 7799.0),
                mutableListOf<Double>(0.99, 1.13, 0.87),
            ),
            PrimaryRestBreakNoticeOdometerOffsetAdjuster(0.66),
            PercentPrimaryRestBreakNoticeAdjuster(-18.4),
            MinimumPrimaryRestBreakNoticeAdjuster(35.9),
            NamePrimaryRestBreakNoticeAdjuster(
                mutableMapOf<String, Double>("retail" to 0.83, "north" to 1.3, "south" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 76677.8,
    )
}
