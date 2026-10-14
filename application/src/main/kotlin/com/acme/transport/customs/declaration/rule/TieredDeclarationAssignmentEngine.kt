package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered declaration assignment calculation. */
interface TieredDeclarationAssignmentFactor {
    fun describe(): String
    fun adjust(item: TieredDeclarationAssignment, value: Double): Double
}

data class MinimumTieredDeclarationAssignmentAdjuster(var minimum: Double) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTieredDeclarationAssignmentAdjuster(var step: Double) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentTieredDeclarationAssignmentAdjuster(var percent: Double) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationAssignmentOdometerOffsetAdjuster(
    var weight: Double,
) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class FlaggedTieredDeclarationAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredDeclarationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDeclarationAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDeclarationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered declaration assignment steps in order and keeps a trace of each. */
data class TieredDeclarationAssignmentEngine(
    var steps: MutableList<TieredDeclarationAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredDeclarationAssignment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDeclarationAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDeclarationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredDeclarationAssignmentEngine(flags: Registry): TieredDeclarationAssignmentEngine {
    return TieredDeclarationAssignmentEngine(
        steps = mutableListOf<TieredDeclarationAssignmentFactor>(
            MinimumTieredDeclarationAssignmentAdjuster(11.4),
            RoundingTieredDeclarationAssignmentAdjuster(1.0),
            PercentTieredDeclarationAssignmentAdjuster(6.8),
            TieredDeclarationAssignmentOdometerOffsetAdjuster(1.03),
            FlaggedTieredDeclarationAssignmentAdjuster(flags, "tiered-declaration-assignment-scheduled", 9.9),
            TieredTieredDeclarationAssignmentAdjuster(
                mutableListOf<Double>(2189.0, 7422.0),
                mutableListOf<Double>(0.83, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 58755.7,
    )
}
