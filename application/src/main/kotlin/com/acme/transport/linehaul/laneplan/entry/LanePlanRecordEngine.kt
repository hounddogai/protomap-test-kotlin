package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan record calculation. */
interface LanePlanRecordAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanRecord, value: Double): Double
}

data class PercentLanePlanRecordAdjuster(var percent: Double) : LanePlanRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLanePlanRecordAdjuster(var minimum: Double) : LanePlanRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLanePlanRecordAdjuster(var cap: Double) : LanePlanRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingLanePlanRecordAdjuster(var step: Double) : LanePlanRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanRecord, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLanePlanRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lane plan record steps in order and keeps a trace of each. */
data class LanePlanRecordEngine(
    var steps: MutableList<LanePlanRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanRecord): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanRecordEngine(flags: Registry): LanePlanRecordEngine {
    return LanePlanRecordEngine(
        steps = mutableListOf<LanePlanRecordAdjuster>(
            PercentLanePlanRecordAdjuster(17.9),
            MinimumLanePlanRecordAdjuster(29.3),
            CapLanePlanRecordAdjuster(42369.0),
            RoundingLanePlanRecordAdjuster(1.0),
            FlaggedLanePlanRecordAdjuster(flags, "lane-plan-record-global", 8.6),
            TieredLanePlanRecordAdjuster(
                mutableListOf<Double>(457.0, 2445.0, 3808.0, 8276.0),
                mutableListOf<Double>(0.89, 1.06, 0.85, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 77527.2,
    )
}
