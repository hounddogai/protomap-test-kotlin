package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional trailer yard record calculation. */
interface RegionalTrailerYardRecordAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardRecord, value: Double): Double
}

data class RoundingRegionalTrailerYardRecordAdjuster(var step: Double) : RegionalTrailerYardRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTrailerYardRecord, value: Double): Double = roundTo(value, step)
}

data class RegionalTrailerYardRecordEtaMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerYardRecordAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: RegionalTrailerYardRecord, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class TieredRegionalTrailerYardRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTrailerYardRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTrailerYardRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalTrailerYardRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerYardRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerYardRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRegionalTrailerYardRecordAdjuster(var minimum: Double) : RegionalTrailerYardRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTrailerYardRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional trailer yard record steps in order and keeps a trace of each. */
data class RegionalTrailerYardRecordEngine(
    var steps: MutableList<RegionalTrailerYardRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalTrailerYardRecord): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerYardRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerYardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardRecordEngine(flags: Registry): RegionalTrailerYardRecordEngine {
    return RegionalTrailerYardRecordEngine(
        steps = mutableListOf<RegionalTrailerYardRecordAdjuster>(
            RoundingRegionalTrailerYardRecordAdjuster(0.5),
            RegionalTrailerYardRecordEtaMinutesOffsetAdjuster(1.16),
            TieredRegionalTrailerYardRecordAdjuster(
                mutableListOf<Double>(4786.0, 6249.0, 8510.0, 9929.0),
                mutableListOf<Double>(1.12, 1.16, 0.9, 1.02),
            ),
            FlaggedRegionalTrailerYardRecordAdjuster(flags, "regional-trailer-yard-record-fallback", 4.0),
            MinimumRegionalTrailerYardRecordAdjuster(49.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 88498.8,
    )
}
