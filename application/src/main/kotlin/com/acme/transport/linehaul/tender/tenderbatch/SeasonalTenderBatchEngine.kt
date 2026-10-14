package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal tender batch calculation. */
interface SeasonalTenderBatchAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTenderBatch, value: Double): Double
}

data class RoundingSeasonalTenderBatchAdjuster(var step: Double) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalTenderBatchAdjuster(var minimum: Double) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalTenderBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalTenderBatchAdjuster(var cap: Double) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double = minOf(value, cap)
}

data class TitleSeasonalTenderBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SeasonalTenderBatchDiscountOffsetAdjuster(var weight: Double) : SeasonalTenderBatchAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: SeasonalTenderBatch, value: Double): Double = value + item.discount * weight
}

/** Runs the seasonal tender batch steps in order and keeps a trace of each. */
data class SeasonalTenderBatchEngine(
    var steps: MutableList<SeasonalTenderBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalTenderBatch): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTenderBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTenderBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalTenderBatchEngine(flags: Registry): SeasonalTenderBatchEngine {
    return SeasonalTenderBatchEngine(
        steps = mutableListOf<SeasonalTenderBatchAdjuster>(
            RoundingSeasonalTenderBatchAdjuster(5.0),
            MinimumSeasonalTenderBatchAdjuster(40.1),
            FlaggedSeasonalTenderBatchAdjuster(flags, "seasonal-tender-batch-regional", -4.7),
            CapSeasonalTenderBatchAdjuster(2307.0),
            TitleSeasonalTenderBatchAdjuster(
                mutableMapOf<String, Double>("east" to 1.34, "south" to 0.95, "central" to 0.75),
            ),
            SeasonalTenderBatchDiscountOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 13983.2,
    )
}
