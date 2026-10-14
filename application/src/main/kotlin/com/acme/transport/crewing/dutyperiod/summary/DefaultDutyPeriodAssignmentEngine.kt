package com.acme.transport.crewing.dutyperiod.summary

/** One step of the default duty period assignment calculation. */
interface DefaultDutyPeriodAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultDutyPeriodAssignment, value: Double): Double
}

data class MinimumDefaultDutyPeriodAssignmentAdjuster(
    var minimum: Double,
) : DefaultDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDutyPeriodAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultDutyPeriodAssignmentEndsAtOffsetAdjuster(
    var weight: Double,
) : DefaultDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DefaultDutyPeriodAssignment, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class PercentDefaultDutyPeriodAssignmentAdjuster(
    var percent: Double,
) : DefaultDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDutyPeriodAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelDefaultDutyPeriodAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultDutyPeriodAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default duty period assignment steps in order and keeps a trace of each. */
data class DefaultDutyPeriodAssignmentEngine(
    var steps: MutableList<DefaultDutyPeriodAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultDutyPeriodAssignment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDutyPeriodAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDutyPeriodAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultDutyPeriodAssignmentEngine(): DefaultDutyPeriodAssignmentEngine {
    return DefaultDutyPeriodAssignmentEngine(
        steps = mutableListOf<DefaultDutyPeriodAssignmentAdjuster>(
            MinimumDefaultDutyPeriodAssignmentAdjuster(46.3),
            DefaultDutyPeriodAssignmentEndsAtOffsetAdjuster(1.47),
            PercentDefaultDutyPeriodAssignmentAdjuster(21.2),
            LabelDefaultDutyPeriodAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.12, "central" to 1.1, "retail" to 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 7739.7,
    )
}
