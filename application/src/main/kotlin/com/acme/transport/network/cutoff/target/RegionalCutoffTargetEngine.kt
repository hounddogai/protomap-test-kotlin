package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional cutoff target calculation. */
interface RegionalCutoffTargetFactor {
    fun describe(): String
    fun adjust(item: RegionalCutoffTarget, value: Double): Double
}

data class RegionalCutoffTargetCreatedAtOffsetAdjuster(var weight: Double) : RegionalCutoffTargetFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RegionalCutoffTarget, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class FlaggedRegionalCutoffTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCutoffTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCutoffTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalCutoffTargetAdjuster(var cap: Double) : RegionalCutoffTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCutoffTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalCutoffTargetAdjuster(var minimum: Double) : RegionalCutoffTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCutoffTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalCutoffTargetAdjuster(var percent: Double) : RegionalCutoffTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCutoffTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional cutoff target steps in order and keeps a trace of each. */
data class RegionalCutoffTargetEngine(
    var steps: MutableList<RegionalCutoffTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCutoffTarget): Double {
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

    fun evaluateAll(items: MutableList<RegionalCutoffTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCutoffTargetEngine(flags: Registry): RegionalCutoffTargetEngine {
    return RegionalCutoffTargetEngine(
        steps = mutableListOf<RegionalCutoffTargetFactor>(
            RegionalCutoffTargetCreatedAtOffsetAdjuster(1.39),
            FlaggedRegionalCutoffTargetAdjuster(flags, "regional-cutoff-target-batched", 7.1),
            CapRegionalCutoffTargetAdjuster(47640.0),
            MinimumRegionalCutoffTargetAdjuster(43.4),
            PercentRegionalCutoffTargetAdjuster(-11.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 47090.6,
    )
}
