package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration slot calculation. */
interface DeclarationSlotAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationSlot, value: Double): Double
}

data class DeclarationSlotQuantityOffsetAdjuster(var weight: Double) : DeclarationSlotAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DeclarationSlot, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class TieredDeclarationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDeclarationSlotAdjuster(var minimum: Double) : DeclarationSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDeclarationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDeclarationSlotAdjuster(var percent: Double) : DeclarationSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the declaration slot steps in order and keeps a trace of each. */
data class DeclarationSlotEngine(
    var steps: MutableList<DeclarationSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationSlot): Double {
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

    fun evaluateAll(items: MutableList<DeclarationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationSlotEngine(flags: Registry): DeclarationSlotEngine {
    return DeclarationSlotEngine(
        steps = mutableListOf<DeclarationSlotAdjuster>(
            DeclarationSlotQuantityOffsetAdjuster(1.69),
            TieredDeclarationSlotAdjuster(
                mutableListOf<Double>(150.0, 3689.0, 4676.0, 8052.0),
                mutableListOf<Double>(1.13, 1.07, 1.07, 1.16),
            ),
            MinimumDeclarationSlotAdjuster(38.2),
            FlaggedDeclarationSlotAdjuster(flags, "declaration-slot-scheduled", -8.5),
            PercentDeclarationSlotAdjuster(5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 19139.5,
    )
}
