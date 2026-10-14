package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary clearance group calculation. */
interface PrimaryClearanceGroupFactor {
    fun describe(): String
    fun adjust(item: PrimaryClearanceGroup, value: Double): Double
}

data class RoundingPrimaryClearanceGroupAdjuster(var step: Double) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double = roundTo(value, step)
}

data class PrimaryClearanceGroupBudgetOffsetAdjuster(var weight: Double) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double = value + item.budget * weight
}

data class CapPrimaryClearanceGroupAdjuster(var cap: Double) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryClearanceGroupAdjuster(var minimum: Double) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryClearanceGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NamePrimaryClearanceGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryClearanceGroupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryClearanceGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary clearance group steps in order and keeps a trace of each. */
data class PrimaryClearanceGroupEngine(
    var steps: MutableList<PrimaryClearanceGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryClearanceGroup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryClearanceGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryClearanceGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryClearanceGroupEngine(flags: Registry): PrimaryClearanceGroupEngine {
    return PrimaryClearanceGroupEngine(
        steps = mutableListOf<PrimaryClearanceGroupFactor>(
            RoundingPrimaryClearanceGroupAdjuster(0.25),
            PrimaryClearanceGroupBudgetOffsetAdjuster(0.05),
            CapPrimaryClearanceGroupAdjuster(41299.0),
            MinimumPrimaryClearanceGroupAdjuster(6.1),
            FlaggedPrimaryClearanceGroupAdjuster(flags, "primary-clearance-group-rolling", 6.9),
            NamePrimaryClearanceGroupAdjuster(mutableMapOf<String, Double>("north" to 0.94, "west" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 16374.3,
    )
}
