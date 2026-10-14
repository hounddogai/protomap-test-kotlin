package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped dock door tier calculation. */
interface CappedDockDoorTierStep {
    fun describe(): String
    fun adjust(item: CappedDockDoorTier, value: Double): Double
}

data class RegionCappedDockDoorTierAdjuster(var factors: MutableMap<String, Double>) : CappedDockDoorTierStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedDockDoorTierAdjuster(var cap: Double) : CappedDockDoorTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double = minOf(value, cap)
}

data class TieredCappedDockDoorTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDockDoorTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedDockDoorTierAdjuster(var step: Double) : CappedDockDoorTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedDockDoorTierAdjuster(var minimum: Double) : CappedDockDoorTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedDockDoorTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDockDoorTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDockDoorTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped dock door tier steps in order and keeps a trace of each. */
data class CappedDockDoorTierEngine(
    var steps: MutableList<CappedDockDoorTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDockDoorTier): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDockDoorTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDockDoorTierEngine(flags: Registry): CappedDockDoorTierEngine {
    return CappedDockDoorTierEngine(
        steps = mutableListOf<CappedDockDoorTierStep>(
            RegionCappedDockDoorTierAdjuster(mutableMapOf<String, Double>("east" to 1.22, "central" to 1.24)),
            CapCappedDockDoorTierAdjuster(24830.0),
            TieredCappedDockDoorTierAdjuster(
                mutableListOf<Double>(1191.0, 9539.0, 9804.0),
                mutableListOf<Double>(0.93, 0.97, 1.09),
            ),
            RoundingCappedDockDoorTierAdjuster(0.25),
            MinimumCappedDockDoorTierAdjuster(3.5),
            FlaggedCappedDockDoorTierAdjuster(flags, "capped-dock-door-tier-weighted", -7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 9240.6,
    )
}
