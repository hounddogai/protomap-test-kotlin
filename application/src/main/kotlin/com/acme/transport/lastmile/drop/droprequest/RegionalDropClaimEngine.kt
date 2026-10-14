package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional drop claim calculation. */
interface RegionalDropClaimAdjuster {
    fun describe(): String
    fun adjust(item: RegionalDropClaim, value: Double): Double
}

data class CapRegionalDropClaimAdjuster(var cap: Double) : RegionalDropClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDropClaim, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalDropClaimAdjuster(var step: Double) : RegionalDropClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDropClaim, value: Double): Double = roundTo(value, step)
}

data class NameRegionalDropClaimAdjuster(var factors: MutableMap<String, Double>) : RegionalDropClaimAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalDropClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalDropClaimAdjuster(var minimum: Double) : RegionalDropClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDropClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalDropClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDropClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDropClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalDropClaimStopCountOffsetAdjuster(var weight: Double) : RegionalDropClaimAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RegionalDropClaim, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the regional drop claim steps in order and keeps a trace of each. */
data class RegionalDropClaimEngine(
    var steps: MutableList<RegionalDropClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDropClaim): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalDropClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDropClaimEngine(flags: Registry): RegionalDropClaimEngine {
    return RegionalDropClaimEngine(
        steps = mutableListOf<RegionalDropClaimAdjuster>(
            CapRegionalDropClaimAdjuster(44173.0),
            RoundingRegionalDropClaimAdjuster(5.0),
            NameRegionalDropClaimAdjuster(mutableMapOf<String, Double>("retail" to 0.96)),
            MinimumRegionalDropClaimAdjuster(17.6),
            FlaggedRegionalDropClaimAdjuster(flags, "regional-drop-claim-legacy", -8.2),
            RegionalDropClaimStopCountOffsetAdjuster(1.13),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 36080.5,
    )
}
