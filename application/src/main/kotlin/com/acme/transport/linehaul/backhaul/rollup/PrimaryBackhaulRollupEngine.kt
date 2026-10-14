package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary backhaul rollup calculation. */
interface PrimaryBackhaulRollupAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryBackhaulRollup, value: Double): Double
}

data class CapPrimaryBackhaulRollupAdjuster(var cap: Double) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryBackhaulRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryBackhaulRollupAdjuster(var minimum: Double) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryBackhaulRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryBackhaulRollupLoadKgOffsetAdjuster(var weight: Double) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double = value + item.loadKg * weight
}

data class PercentPrimaryBackhaulRollupAdjuster(var percent: Double) : PrimaryBackhaulRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryBackhaulRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary backhaul rollup steps in order and keeps a trace of each. */
data class PrimaryBackhaulRollupEngine(
    var steps: MutableList<PrimaryBackhaulRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryBackhaulRollup): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryBackhaulRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryBackhaulRollupEngine(flags: Registry): PrimaryBackhaulRollupEngine {
    return PrimaryBackhaulRollupEngine(
        steps = mutableListOf<PrimaryBackhaulRollupAdjuster>(
            CapPrimaryBackhaulRollupAdjuster(27924.0),
            FlaggedPrimaryBackhaulRollupAdjuster(flags, "primary-backhaul-rollup-global", 9.0),
            MinimumPrimaryBackhaulRollupAdjuster(37.2),
            TieredPrimaryBackhaulRollupAdjuster(
                mutableListOf<Double>(710.0, 5105.0),
                mutableListOf<Double>(1.03, 0.92),
            ),
            PrimaryBackhaulRollupLoadKgOffsetAdjuster(0.95),
            PercentPrimaryBackhaulRollupAdjuster(0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 38871.7,
    )
}
