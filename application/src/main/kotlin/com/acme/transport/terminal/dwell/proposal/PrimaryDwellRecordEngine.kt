package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary dwell record calculation. */
interface PrimaryDwellRecordAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDwellRecord, value: Double): Double
}

data class TieredPrimaryDwellRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryDwellRecordAdjuster(var cap: Double) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryDwellRecordAdjuster(var percent: Double) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryDwellRecordAdjuster(var minimum: Double) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryDwellRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryDwellRecordAdjuster(var step: Double) : PrimaryDwellRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDwellRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the primary dwell record steps in order and keeps a trace of each. */
data class PrimaryDwellRecordEngine(
    var steps: MutableList<PrimaryDwellRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryDwellRecord): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDwellRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDwellRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryDwellRecordEngine(flags: Registry): PrimaryDwellRecordEngine {
    return PrimaryDwellRecordEngine(
        steps = mutableListOf<PrimaryDwellRecordAdjuster>(
            TieredPrimaryDwellRecordAdjuster(
                mutableListOf<Double>(4341.0, 5556.0, 6416.0, 9795.0),
                mutableListOf<Double>(1.1, 1.04, 0.98, 0.85),
            ),
            CapPrimaryDwellRecordAdjuster(12247.0),
            PercentPrimaryDwellRecordAdjuster(-18.9),
            MinimumPrimaryDwellRecordAdjuster(25.7),
            FlaggedPrimaryDwellRecordAdjuster(flags, "primary-dwell-record-regional", 8.1),
            RoundingPrimaryDwellRecordAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 42902.9,
    )
}
