package com.acme.transport.crewing.dutyperiod.assignment

/** One step of the duty period policy calculation. */
interface DutyPeriodPolicyAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodPolicy, value: Double): Double
}

data class LabelDutyPeriodPolicyAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodPolicyAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DutyPeriodPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDutyPeriodPolicyAdjuster(var minimum: Double) : DutyPeriodPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DutyPeriodPolicyDurationMinutesOffsetAdjuster(var weight: Double) : DutyPeriodPolicyAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DutyPeriodPolicy, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the duty period policy steps in order and keeps a trace of each. */
data class DutyPeriodPolicyEngine(
    var steps: MutableList<DutyPeriodPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DutyPeriodPolicy): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDutyPeriodPolicyEngine(): DutyPeriodPolicyEngine {
    return DutyPeriodPolicyEngine(
        steps = mutableListOf<DutyPeriodPolicyAdjuster>(
            LabelDutyPeriodPolicyAdjuster(mutableMapOf<String, Double>("central" to 1.19, "south" to 1.26)),
            MinimumDutyPeriodPolicyAdjuster(13.0),
            DutyPeriodPolicyDurationMinutesOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 77813.0,
    )
}
