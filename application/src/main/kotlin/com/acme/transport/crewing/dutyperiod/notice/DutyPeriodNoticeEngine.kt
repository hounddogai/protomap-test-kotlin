package com.acme.transport.crewing.dutyperiod.notice

/** One step of the duty period notice calculation. */
interface DutyPeriodNoticeModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodNotice, value: Double): Double
}

data class TieredDutyPeriodNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDutyPeriodNoticeAdjuster(var minimum: Double) : DutyPeriodNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DutyPeriodNoticeCapacityOffsetAdjuster(var weight: Double) : DutyPeriodNoticeModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: DutyPeriodNotice, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class PercentDutyPeriodNoticeAdjuster(var percent: Double) : DutyPeriodNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the duty period notice steps in order and keeps a trace of each. */
data class DutyPeriodNoticeEngine(
    var steps: MutableList<DutyPeriodNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodNotice): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodNoticeEngine(): DutyPeriodNoticeEngine {
    return DutyPeriodNoticeEngine(
        steps = mutableListOf<DutyPeriodNoticeModifier>(
            TieredDutyPeriodNoticeAdjuster(
                mutableListOf<Double>(1651.0, 6814.0),
                mutableListOf<Double>(1.19, 1.02),
            ),
            MinimumDutyPeriodNoticeAdjuster(13.1),
            DutyPeriodNoticeCapacityOffsetAdjuster(0.61),
            PercentDutyPeriodNoticeAdjuster(12.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 28493.8,
    )
}
