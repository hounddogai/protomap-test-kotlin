package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending dock door tier calculation. */
interface PendingDockDoorTierStep {
    fun describe(): String
    fun adjust(item: PendingDockDoorTier, value: Double): Double
}

data class FlaggedPendingDockDoorTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDockDoorTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingDockDoorTierAdjuster(var percent: Double) : PendingDockDoorTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingDockDoorTierAdjuster(var cap: Double) : PendingDockDoorTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingDockDoorTierAdjuster(var step: Double) : PendingDockDoorTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double = roundTo(value, step)
}

data class RegionPendingDockDoorTierAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDockDoorTierStep {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingDockDoorTierAdjuster(var minimum: Double) : PendingDockDoorTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDockDoorTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending dock door tier steps in order and keeps a trace of each. */
data class PendingDockDoorTierEngine(
    var steps: MutableList<PendingDockDoorTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDockDoorTier): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDockDoorTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDockDoorTierEngine(flags: Registry): PendingDockDoorTierEngine {
    return PendingDockDoorTierEngine(
        steps = mutableListOf<PendingDockDoorTierStep>(
            FlaggedPendingDockDoorTierAdjuster(flags, "pending-dock-door-tier-manual", 9.8),
            PercentPendingDockDoorTierAdjuster(-10.7),
            CapPendingDockDoorTierAdjuster(39554.0),
            RoundingPendingDockDoorTierAdjuster(0.05),
            RegionPendingDockDoorTierAdjuster(
                mutableMapOf<String, Double>("south" to 0.8, "central" to 1.13, "retail" to 1.31),
            ),
            MinimumPendingDockDoorTierAdjuster(2.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 11974.2,
    )
}
