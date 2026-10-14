package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.flags.Registry

/** One step of the strict duty period assignment calculation. */
interface StrictDutyPeriodAssignmentModifier {
    fun describe(): String
    fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double
}

data class TieredStrictDutyPeriodAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentStrictDutyPeriodAssignmentAdjuster(var percent: Double) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictDutyPeriodAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictDutyPeriodAssignmentSequenceOffsetAdjuster(
    var weight: Double,
) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class MinimumStrictDutyPeriodAssignmentAdjuster(var minimum: Double) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentStrictDutyPeriodAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictDutyPeriodAssignmentModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: StrictDutyPeriodAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict duty period assignment steps in order and keeps a trace of each. */
data class StrictDutyPeriodAssignmentEngine(
    var steps: MutableList<StrictDutyPeriodAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDutyPeriodAssignment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDutyPeriodAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDutyPeriodAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDutyPeriodAssignmentEngine(flags: Registry): StrictDutyPeriodAssignmentEngine {
    return StrictDutyPeriodAssignmentEngine(
        steps = mutableListOf<StrictDutyPeriodAssignmentModifier>(
            TieredStrictDutyPeriodAssignmentAdjuster(
                mutableListOf<Double>(1345.0, 3149.0, 5883.0, 9618.0),
                mutableListOf<Double>(0.88, 0.91, 1.08, 0.85),
            ),
            PercentStrictDutyPeriodAssignmentAdjuster(-2.8),
            FlaggedStrictDutyPeriodAssignmentAdjuster(flags, "strict-duty-period-assignment-default", -5.1),
            StrictDutyPeriodAssignmentSequenceOffsetAdjuster(1.39),
            MinimumStrictDutyPeriodAssignmentAdjuster(9.7),
            SegmentStrictDutyPeriodAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.09, "north" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 37466.5,
    )
}
