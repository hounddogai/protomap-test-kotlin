package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster variance calculation. */
interface RosterVarianceFactor {
    fun describe(): String
    fun adjust(item: RosterVariance, value: Double): Double
}

data class FlaggedRosterVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRosterVarianceAdjuster(var cap: Double) : RosterVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterVariance, value: Double): Double = minOf(value, cap)
}

data class TieredRosterVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster variance steps in order and keeps a trace of each. */
data class RosterVarianceEngine(
    var steps: MutableList<RosterVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterVariance): Double {
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

    fun evaluateAll(items: MutableList<RosterVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterVarianceEngine(flags: Registry): RosterVarianceEngine {
    return RosterVarianceEngine(
        steps = mutableListOf<RosterVarianceFactor>(
            FlaggedRosterVarianceAdjuster(flags, "roster-variance-regional", -1.6),
            CapRosterVarianceAdjuster(5931.0),
            TieredRosterVarianceAdjuster(
                mutableListOf<Double>(2467.0, 6209.0, 6778.0),
                mutableListOf<Double>(1.15, 1.01, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 53378.5,
    )
}
