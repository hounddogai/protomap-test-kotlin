package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending duty period preference calculation. */
interface PendingDutyPeriodPreferenceModifier {
    fun describe(): String
    fun adjust(item: PendingDutyPeriodPreference, value: Double): Double
}

data class RoundingPendingDutyPeriodPreferenceAdjuster(var step: Double) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double = roundTo(value, step)
}

data class CapPendingDutyPeriodPreferenceAdjuster(var cap: Double) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double = minOf(value, cap)
}

data class ChannelPendingDutyPeriodPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDutyPeriodPreferenceAdjuster(
    var percent: Double,
) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingDutyPeriodPreferenceAdjuster(
    var minimum: Double,
) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingDutyPeriodPreferenceRevisionOffsetAdjuster(
    var weight: Double,
) : PendingDutyPeriodPreferenceModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PendingDutyPeriodPreference, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the pending duty period preference steps in order and keeps a trace of each. */
data class PendingDutyPeriodPreferenceEngine(
    var steps: MutableList<PendingDutyPeriodPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDutyPeriodPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDutyPeriodPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDutyPeriodPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDutyPeriodPreferenceEngine(): PendingDutyPeriodPreferenceEngine {
    return PendingDutyPeriodPreferenceEngine(
        steps = mutableListOf<PendingDutyPeriodPreferenceModifier>(
            RoundingPendingDutyPeriodPreferenceAdjuster(0.5),
            CapPendingDutyPeriodPreferenceAdjuster(576.0),
            ChannelPendingDutyPeriodPreferenceAdjuster(mutableMapOf<String, Double>("retail" to 0.73)),
            PercentPendingDutyPeriodPreferenceAdjuster(-12.1),
            MinimumPendingDutyPeriodPreferenceAdjuster(49.8),
            PendingDutyPeriodPreferenceRevisionOffsetAdjuster(0.59),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 67191.1,
    )
}
