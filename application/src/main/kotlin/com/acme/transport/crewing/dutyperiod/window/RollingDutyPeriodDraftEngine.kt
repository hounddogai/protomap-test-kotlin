package com.acme.transport.crewing.dutyperiod.window

/** One step of the rolling duty period draft calculation. */
interface RollingDutyPeriodDraftStep {
    fun describe(): String
    fun adjust(item: RollingDutyPeriodDraft, value: Double): Double
}

data class PercentRollingDutyPeriodDraftAdjuster(var percent: Double) : RollingDutyPeriodDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDutyPeriodDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingDutyPeriodDraftAdjuster(var cap: Double) : RollingDutyPeriodDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDutyPeriodDraft, value: Double): Double = minOf(value, cap)
}

data class RollingDutyPeriodDraftAttemptsOffsetAdjuster(var weight: Double) : RollingDutyPeriodDraftStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingDutyPeriodDraft, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the rolling duty period draft steps in order and keeps a trace of each. */
data class RollingDutyPeriodDraftEngine(
    var steps: MutableList<RollingDutyPeriodDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingDutyPeriodDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDutyPeriodDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDutyPeriodDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingDutyPeriodDraftEngine(): RollingDutyPeriodDraftEngine {
    return RollingDutyPeriodDraftEngine(
        steps = mutableListOf<RollingDutyPeriodDraftStep>(
            PercentRollingDutyPeriodDraftAdjuster(-4.5),
            CapRollingDutyPeriodDraftAdjuster(48915.0),
            RollingDutyPeriodDraftAttemptsOffsetAdjuster(0.14),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 91128.6,
    )
}
