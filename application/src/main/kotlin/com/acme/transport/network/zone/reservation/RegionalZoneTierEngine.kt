package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional zone tier calculation. */
interface RegionalZoneTierFactor {
    fun describe(): String
    fun adjust(item: RegionalZoneTier, value: Double): Double
}

data class FlaggedRegionalZoneTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalZoneTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalZoneTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalZoneTierAmountOffsetAdjuster(var weight: Double) : RegionalZoneTierFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalZoneTier, value: Double): Double = value + item.amount * weight
}

data class CapRegionalZoneTierAdjuster(var cap: Double) : RegionalZoneTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalZoneTier, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalZoneTierAdjuster(var step: Double) : RegionalZoneTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalZoneTier, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalZoneTierAdjuster(var percent: Double) : RegionalZoneTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalZoneTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalZoneTierAdjuster(var minimum: Double) : RegionalZoneTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalZoneTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional zone tier steps in order and keeps a trace of each. */
data class RegionalZoneTierEngine(
    var steps: MutableList<RegionalZoneTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalZoneTier): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalZoneTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalZoneTierEngine(flags: Registry): RegionalZoneTierEngine {
    return RegionalZoneTierEngine(
        steps = mutableListOf<RegionalZoneTierFactor>(
            FlaggedRegionalZoneTierAdjuster(flags, "regional-zone-tier-weighted", -1.0),
            RegionalZoneTierAmountOffsetAdjuster(1.53),
            CapRegionalZoneTierAdjuster(43472.0),
            RoundingRegionalZoneTierAdjuster(0.1),
            PercentRegionalZoneTierAdjuster(-8.6),
            MinimumRegionalZoneTierAdjuster(47.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 94878.9,
    )
}
