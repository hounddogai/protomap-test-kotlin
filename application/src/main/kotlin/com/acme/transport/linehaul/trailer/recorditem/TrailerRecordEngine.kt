package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the trailer record calculation. */
interface TrailerRecordModifier {
    fun describe(): String
    fun adjust(item: TrailerRecord, value: Double): Double
}

data class TieredTrailerRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTrailerRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerRecordDurationMinutesOffsetAdjuster(var weight: Double) : TrailerRecordModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TrailerRecord, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class CapTrailerRecordAdjuster(var cap: Double) : TrailerRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer record steps in order and keeps a trace of each. */
data class TrailerRecordEngine(
    var steps: MutableList<TrailerRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerRecord): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerRecordEngine(flags: Registry): TrailerRecordEngine {
    return TrailerRecordEngine(
        steps = mutableListOf<TrailerRecordModifier>(
            TieredTrailerRecordAdjuster(
                mutableListOf<Double>(2998.0, 6059.0, 6260.0),
                mutableListOf<Double>(1.05, 1.01, 0.9),
            ),
            FlaggedTrailerRecordAdjuster(flags, "trailer-record-scheduled", -6.2),
            TrailerRecordDurationMinutesOffsetAdjuster(0.28),
            CapTrailerRecordAdjuster(39874.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 35479.0,
    )
}
