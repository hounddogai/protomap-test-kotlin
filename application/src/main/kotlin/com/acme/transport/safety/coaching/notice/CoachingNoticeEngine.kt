package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching notice calculation. */
interface CoachingNoticeStep {
    fun describe(): String
    fun adjust(item: CoachingNotice, value: Double): Double
}

data class RoundingCoachingNoticeAdjuster(var step: Double) : CoachingNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingNotice, value: Double): Double = roundTo(value, step)
}

data class CoachingNoticeBudgetOffsetAdjuster(var weight: Double) : CoachingNoticeStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CoachingNotice, value: Double): Double = value + item.budget * weight
}

data class PercentCoachingNoticeAdjuster(var percent: Double) : CoachingNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryCoachingNoticeAdjuster(var factors: MutableMap<String, Double>) : CoachingNoticeStep {
    override fun describe(): String = "by category"

    override fun adjust(item: CoachingNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the coaching notice steps in order and keeps a trace of each. */
data class CoachingNoticeEngine(
    var steps: MutableList<CoachingNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingNotice): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingNoticeEngine(): CoachingNoticeEngine {
    return CoachingNoticeEngine(
        steps = mutableListOf<CoachingNoticeStep>(
            RoundingCoachingNoticeAdjuster(1.0),
            CoachingNoticeBudgetOffsetAdjuster(0.86),
            PercentCoachingNoticeAdjuster(9.6),
            CategoryCoachingNoticeAdjuster(mutableMapOf<String, Double>("online" to 1.32, "west" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 52837.6,
    )
}
