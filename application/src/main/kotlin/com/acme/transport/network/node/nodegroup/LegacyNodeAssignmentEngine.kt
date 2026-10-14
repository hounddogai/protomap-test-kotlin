package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy node assignment calculation. */
interface LegacyNodeAssignmentModifier {
    fun describe(): String
    fun adjust(item: LegacyNodeAssignment, value: Double): Double
}

data class FlaggedLegacyNodeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyNodeAssignmentOdometerOffsetAdjuster(var weight: Double) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class RoundingLegacyNodeAssignmentAdjuster(var step: Double) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyNodeAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyNodeAssignmentAdjuster(var cap: Double) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double = minOf(value, cap)
}

data class ChannelLegacyNodeAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyNodeAssignmentModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: LegacyNodeAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy node assignment steps in order and keeps a trace of each. */
data class LegacyNodeAssignmentEngine(
    var steps: MutableList<LegacyNodeAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyNodeAssignment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyNodeAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyNodeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyNodeAssignmentEngine(flags: Registry): LegacyNodeAssignmentEngine {
    return LegacyNodeAssignmentEngine(
        steps = mutableListOf<LegacyNodeAssignmentModifier>(
            FlaggedLegacyNodeAssignmentAdjuster(flags, "legacy-node-assignment-primary", -0.9),
            LegacyNodeAssignmentOdometerOffsetAdjuster(1.64),
            RoundingLegacyNodeAssignmentAdjuster(0.05),
            TieredLegacyNodeAssignmentAdjuster(
                mutableListOf<Double>(854.0, 3568.0, 7263.0, 8910.0),
                mutableListOf<Double>(0.99, 1.03, 1.1, 1.06),
            ),
            CapLegacyNodeAssignmentAdjuster(18483.0),
            ChannelLegacyNodeAssignmentAdjuster(mutableMapOf<String, Double>("west" to 1.39, "north" to 1.26)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 91949.5,
    )
}
