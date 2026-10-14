package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional declaration slot calculation. */
interface RegionalDeclarationSlotFactor {
    fun describe(): String
    fun adjust(item: RegionalDeclarationSlot, value: Double): Double
}

data class PercentRegionalDeclarationSlotAdjuster(var percent: Double) : RegionalDeclarationSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDeclarationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalDeclarationSlotAdjuster(var cap: Double) : RegionalDeclarationSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDeclarationSlot, value: Double): Double = minOf(value, cap)
}

data class RegionalDeclarationSlotPalletsOffsetAdjuster(var weight: Double) : RegionalDeclarationSlotFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RegionalDeclarationSlot, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class FlaggedRegionalDeclarationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDeclarationSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDeclarationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalDeclarationSlotAdjuster(var step: Double) : RegionalDeclarationSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDeclarationSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the regional declaration slot steps in order and keeps a trace of each. */
data class RegionalDeclarationSlotEngine(
    var steps: MutableList<RegionalDeclarationSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalDeclarationSlot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDeclarationSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDeclarationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalDeclarationSlotEngine(flags: Registry): RegionalDeclarationSlotEngine {
    return RegionalDeclarationSlotEngine(
        steps = mutableListOf<RegionalDeclarationSlotFactor>(
            PercentRegionalDeclarationSlotAdjuster(-9.7),
            CapRegionalDeclarationSlotAdjuster(5843.0),
            RegionalDeclarationSlotPalletsOffsetAdjuster(0.58),
            FlaggedRegionalDeclarationSlotAdjuster(flags, "regional-declaration-slot-batched", 2.5),
            RoundingRegionalDeclarationSlotAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 64461.0,
    )
}
