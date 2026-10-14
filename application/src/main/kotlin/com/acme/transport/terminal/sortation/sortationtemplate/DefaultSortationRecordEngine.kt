package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.Registry

/** One step of the default sortation record calculation. */
interface DefaultSortationRecordStep {
    fun describe(): String
    fun adjust(item: DefaultSortationRecord, value: Double): Double
}

data class FlaggedDefaultSortationRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultSortationRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultSortationRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultSortationRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultSortationRecordAdjuster(var minimum: Double) : DefaultSortationRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultSortationRecordBatchSizeOffsetAdjuster(var weight: Double) : DefaultSortationRecordStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapDefaultSortationRecordAdjuster(var cap: Double) : DefaultSortationRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultSortationRecordAdjuster(var percent: Double) : DefaultSortationRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSortationRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default sortation record steps in order and keeps a trace of each. */
data class DefaultSortationRecordEngine(
    var steps: MutableList<DefaultSortationRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultSortationRecord): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultSortationRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultSortationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultSortationRecordEngine(flags: Registry): DefaultSortationRecordEngine {
    return DefaultSortationRecordEngine(
        steps = mutableListOf<DefaultSortationRecordStep>(
            FlaggedDefaultSortationRecordAdjuster(flags, "default-sortation-record-fallback", -1.5),
            TieredDefaultSortationRecordAdjuster(
                mutableListOf<Double>(4544.0, 4614.0, 5960.0, 9602.0),
                mutableListOf<Double>(0.94, 0.87, 0.93, 1.15),
            ),
            MinimumDefaultSortationRecordAdjuster(17.6),
            DefaultSortationRecordBatchSizeOffsetAdjuster(0.74),
            CapDefaultSortationRecordAdjuster(34409.0),
            PercentDefaultSortationRecordAdjuster(-18.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 24624.3,
    )
}
