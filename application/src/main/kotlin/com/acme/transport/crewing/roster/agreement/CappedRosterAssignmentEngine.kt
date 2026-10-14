package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped roster assignment calculation. */
interface CappedRosterAssignmentModifier {
    fun describe(): String
    fun adjust(item: CappedRosterAssignment, value: Double): Double
}

data class FlaggedCappedRosterAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRosterAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRosterAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedRosterAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRosterAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRosterAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedRosterAssignmentAdjuster(var cap: Double) : CappedRosterAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRosterAssignment, value: Double): Double = minOf(value, cap)
}

data class CappedRosterAssignmentScoreOffsetAdjuster(var weight: Double) : CappedRosterAssignmentModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CappedRosterAssignment, value: Double): Double = value + item.score * weight
}

/** Runs the capped roster assignment steps in order and keeps a trace of each. */
data class CappedRosterAssignmentEngine(
    var steps: MutableList<CappedRosterAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRosterAssignment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRosterAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRosterAssignmentEngine(flags: Registry): CappedRosterAssignmentEngine {
    return CappedRosterAssignmentEngine(
        steps = mutableListOf<CappedRosterAssignmentModifier>(
            FlaggedCappedRosterAssignmentAdjuster(flags, "capped-roster-assignment-pending", 3.0),
            TieredCappedRosterAssignmentAdjuster(
                mutableListOf<Double>(4506.0, 4730.0),
                mutableListOf<Double>(1.17, 1.16),
            ),
            CapCappedRosterAssignmentAdjuster(17359.0),
            CappedRosterAssignmentScoreOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 12630.4,
    )
}
