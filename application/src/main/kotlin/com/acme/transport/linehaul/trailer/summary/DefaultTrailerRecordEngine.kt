package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default trailer record calculation. */
interface DefaultTrailerRecordFactor {
    fun describe(): String
    fun adjust(item: DefaultTrailerRecord, value: Double): Double
}

data class DefaultTrailerRecordEndsAtOffsetAdjuster(var weight: Double) : DefaultTrailerRecordFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class FlaggedDefaultTrailerRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTrailerRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultTrailerRecordAdjuster(var cap: Double) : DefaultTrailerRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultTrailerRecordAdjuster(var step: Double) : DefaultTrailerRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultTrailerRecordAdjuster(var minimum: Double) : DefaultTrailerRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultTrailerRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTrailerRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTrailerRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default trailer record steps in order and keeps a trace of each. */
data class DefaultTrailerRecordEngine(
    var steps: MutableList<DefaultTrailerRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultTrailerRecord): Double {
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

    fun evaluateAll(items: MutableList<DefaultTrailerRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultTrailerRecordEngine(flags: Registry): DefaultTrailerRecordEngine {
    return DefaultTrailerRecordEngine(
        steps = mutableListOf<DefaultTrailerRecordFactor>(
            DefaultTrailerRecordEndsAtOffsetAdjuster(1.24),
            FlaggedDefaultTrailerRecordAdjuster(flags, "default-trailer-record-seasonal", -7.8),
            CapDefaultTrailerRecordAdjuster(10546.0),
            RoundingDefaultTrailerRecordAdjuster(0.25),
            MinimumDefaultTrailerRecordAdjuster(0.8),
            TieredDefaultTrailerRecordAdjuster(
                mutableListOf<Double>(1090.0, 3551.0),
                mutableListOf<Double>(0.92, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 20299.4,
    )
}
