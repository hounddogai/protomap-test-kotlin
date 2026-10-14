package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default dock door tier calculation. */
interface DefaultDockDoorTierStep {
    fun describe(): String
    fun adjust(item: DefaultDockDoorTier, value: Double): Double
}

data class CapDefaultDockDoorTierAdjuster(var cap: Double) : DefaultDockDoorTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDockDoorTier, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultDockDoorTierAdjuster(var step: Double) : DefaultDockDoorTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDockDoorTier, value: Double): Double = roundTo(value, step)
}

data class TieredDefaultDockDoorTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDockDoorTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDockDoorTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultDockDoorTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultDockDoorTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultDockDoorTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default dock door tier steps in order and keeps a trace of each. */
data class DefaultDockDoorTierEngine(
    var steps: MutableList<DefaultDockDoorTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultDockDoorTier): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDockDoorTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDockDoorTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultDockDoorTierEngine(flags: Registry): DefaultDockDoorTierEngine {
    return DefaultDockDoorTierEngine(
        steps = mutableListOf<DefaultDockDoorTierStep>(
            CapDefaultDockDoorTierAdjuster(9827.0),
            RoundingDefaultDockDoorTierAdjuster(0.05),
            TieredDefaultDockDoorTierAdjuster(
                mutableListOf<Double>(2526.0, 6510.0),
                mutableListOf<Double>(1.05, 0.87),
            ),
            FlaggedDefaultDockDoorTierAdjuster(flags, "default-dock-door-tier-capped", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 15759.7,
    )
}
