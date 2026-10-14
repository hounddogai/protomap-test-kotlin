package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the transit time record calculation. */
interface TransitTimeRecordFactor {
    fun describe(): String
    fun adjust(item: TransitTimeRecord, value: Double): Double
}

data class FlaggedTransitTimeRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTransitTimeRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTransitTimeRecordAdjuster(var percent: Double) : TransitTimeRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TransitTimeRecordFloorValueOffsetAdjuster(var weight: Double) : TransitTimeRecordFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: TransitTimeRecord, value: Double): Double = value + item.floorValue * weight
}

data class MinimumTransitTimeRecordAdjuster(var minimum: Double) : TransitTimeRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time record steps in order and keeps a trace of each. */
data class TransitTimeRecordEngine(
    var steps: MutableList<TransitTimeRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeRecord): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeRecordEngine(flags: Registry): TransitTimeRecordEngine {
    return TransitTimeRecordEngine(
        steps = mutableListOf<TransitTimeRecordFactor>(
            FlaggedTransitTimeRecordAdjuster(flags, "transit-time-record-global", 7.3),
            TieredTransitTimeRecordAdjuster(
                mutableListOf<Double>(3751.0, 4138.0, 5309.0, 8473.0),
                mutableListOf<Double>(0.92, 1.12, 0.9, 1.08),
            ),
            PercentTransitTimeRecordAdjuster(7.2),
            TransitTimeRecordFloorValueOffsetAdjuster(0.81),
            MinimumTransitTimeRecordAdjuster(18.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 50925.1,
    )
}
