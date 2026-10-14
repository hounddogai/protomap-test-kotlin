package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional dock door tier calculation. */
interface RegionalDockDoorTierFactor {
    fun describe(): String
    fun adjust(item: RegionalDockDoorTier, value: Double): Double
}

data class RoundingRegionalDockDoorTierAdjuster(var step: Double) : RegionalDockDoorTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDockDoorTier, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalDockDoorTierAdjuster(var percent: Double) : RegionalDockDoorTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDockDoorTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalDockDoorTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDockDoorTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDockDoorTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRegionalDockDoorTierAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDockDoorTierFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalDockDoorTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalDockDoorTierAdjuster(var minimum: Double) : RegionalDockDoorTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDockDoorTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional dock door tier steps in order and keeps a trace of each. */
data class RegionalDockDoorTierEngine(
    var steps: MutableList<RegionalDockDoorTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDockDoorTier): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDockDoorTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDockDoorTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDockDoorTierEngine(flags: Registry): RegionalDockDoorTierEngine {
    return RegionalDockDoorTierEngine(
        steps = mutableListOf<RegionalDockDoorTierFactor>(
            RoundingRegionalDockDoorTierAdjuster(0.25),
            PercentRegionalDockDoorTierAdjuster(0.6),
            FlaggedRegionalDockDoorTierAdjuster(flags, "regional-dock-door-tier-adaptive", -8.3),
            LabelRegionalDockDoorTierAdjuster(
                mutableMapOf<String, Double>("central" to 0.89, "east" to 0.94, "west" to 0.97),
            ),
            MinimumRegionalDockDoorTierAdjuster(37.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 90441.4,
    )
}
