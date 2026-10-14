package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling qualification notice calculation. */
interface RollingQualificationNoticeModifier {
    fun describe(): String
    fun adjust(item: RollingQualificationNotice, value: Double): Double
}

data class PercentRollingQualificationNoticeAdjuster(var percent: Double) : RollingQualificationNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingQualificationNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleRollingQualificationNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingQualificationNoticeModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingQualificationNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingQualificationNoticeAdjuster(var step: Double) : RollingQualificationNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingQualificationNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling qualification notice steps in order and keeps a trace of each. */
data class RollingQualificationNoticeEngine(
    var steps: MutableList<RollingQualificationNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingQualificationNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingQualificationNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingQualificationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingQualificationNoticeEngine(): RollingQualificationNoticeEngine {
    return RollingQualificationNoticeEngine(
        steps = mutableListOf<RollingQualificationNoticeModifier>(
            PercentRollingQualificationNoticeAdjuster(-9.4),
            LocaleRollingQualificationNoticeAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "west" to 0.97, "south" to 0.84),
            ),
            RoundingRollingQualificationNoticeAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 41359.9,
    )
}
