package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration allocation calculation. */
interface DeclarationAllocationFactor {
    fun describe(): String
    fun adjust(item: DeclarationAllocation, value: Double): Double
}

data class NameDeclarationAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationAllocationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DeclarationAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDeclarationAllocationAdjuster(var step: Double) : DeclarationAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDeclarationAllocationAdjuster(var cap: Double) : DeclarationAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationAllocation, value: Double): Double = minOf(value, cap)
}

data class DeclarationAllocationDurationMinutesOffsetAdjuster(
    var weight: Double,
) : DeclarationAllocationFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DeclarationAllocation, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedDeclarationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration allocation steps in order and keeps a trace of each. */
data class DeclarationAllocationEngine(
    var steps: MutableList<DeclarationAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationAllocation): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationAllocationEngine(flags: Registry): DeclarationAllocationEngine {
    return DeclarationAllocationEngine(
        steps = mutableListOf<DeclarationAllocationFactor>(
            NameDeclarationAllocationAdjuster(mutableMapOf<String, Double>("south" to 0.73, "east" to 1.11)),
            RoundingDeclarationAllocationAdjuster(1.0),
            TieredDeclarationAllocationAdjuster(
                mutableListOf<Double>(1077.0, 6739.0, 9091.0, 9530.0),
                mutableListOf<Double>(1.03, 1.13, 0.87, 0.8),
            ),
            CapDeclarationAllocationAdjuster(23002.0),
            DeclarationAllocationDurationMinutesOffsetAdjuster(1.6),
            FlaggedDeclarationAllocationAdjuster(flags, "declaration-allocation-fallback", -4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 25366.7,
    )
}
