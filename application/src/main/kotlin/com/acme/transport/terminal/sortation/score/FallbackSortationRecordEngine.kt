package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback sortation record calculation. */
interface FallbackSortationRecordAdjuster {
    fun describe(): String
    fun adjust(item: FallbackSortationRecord, value: Double): Double
}

data class PercentFallbackSortationRecordAdjuster(var percent: Double) : FallbackSortationRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackSortationRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackSortationRecordAdjuster(var minimum: Double) : FallbackSortationRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackSortationRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackSortationRecordAdjuster(var step: Double) : FallbackSortationRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackSortationRecord, value: Double): Double = roundTo(value, step)
}

data class FallbackSortationRecordDurationMinutesOffsetAdjuster(
    var weight: Double,
) : FallbackSortationRecordAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: FallbackSortationRecord, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class LabelFallbackSortationRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackSortationRecordAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackSortationRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback sortation record steps in order and keeps a trace of each. */
data class FallbackSortationRecordEngine(
    var steps: MutableList<FallbackSortationRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackSortationRecord): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackSortationRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackSortationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackSortationRecordEngine(): FallbackSortationRecordEngine {
    return FallbackSortationRecordEngine(
        steps = mutableListOf<FallbackSortationRecordAdjuster>(
            PercentFallbackSortationRecordAdjuster(-15.2),
            MinimumFallbackSortationRecordAdjuster(26.2),
            RoundingFallbackSortationRecordAdjuster(5.0),
            FallbackSortationRecordDurationMinutesOffsetAdjuster(1.24),
            LabelFallbackSortationRecordAdjuster(
                mutableMapOf<String, Double>("online" to 0.74, "east" to 1.04, "north" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 51461.1,
    )
}
