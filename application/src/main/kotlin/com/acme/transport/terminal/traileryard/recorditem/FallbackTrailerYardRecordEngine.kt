package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback trailer yard record calculation. */
interface FallbackTrailerYardRecordAdjuster {
    fun describe(): String
    fun adjust(item: FallbackTrailerYardRecord, value: Double): Double
}

data class FlaggedFallbackTrailerYardRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackTrailerYardRecordAdjuster(var minimum: Double) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackTrailerYardRecordSequenceOffsetAdjuster(
    var weight: Double,
) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class CapFallbackTrailerYardRecordAdjuster(var cap: Double) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackTrailerYardRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackTrailerYardRecordAdjuster(var percent: Double) : FallbackTrailerYardRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTrailerYardRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback trailer yard record steps in order and keeps a trace of each. */
data class FallbackTrailerYardRecordEngine(
    var steps: MutableList<FallbackTrailerYardRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackTrailerYardRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackTrailerYardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackTrailerYardRecordEngine(flags: Registry): FallbackTrailerYardRecordEngine {
    return FallbackTrailerYardRecordEngine(
        steps = mutableListOf<FallbackTrailerYardRecordAdjuster>(
            FlaggedFallbackTrailerYardRecordAdjuster(flags, "fallback-trailer-yard-record-strict", 4.8),
            MinimumFallbackTrailerYardRecordAdjuster(5.5),
            FallbackTrailerYardRecordSequenceOffsetAdjuster(1.18),
            CapFallbackTrailerYardRecordAdjuster(9605.0),
            TieredFallbackTrailerYardRecordAdjuster(
                mutableListOf<Double>(1699.0, 2551.0, 7550.0, 8250.0),
                mutableListOf<Double>(0.85, 0.83, 1.15, 1.18),
            ),
            PercentFallbackTrailerYardRecordAdjuster(12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 38252.2,
    )
}
