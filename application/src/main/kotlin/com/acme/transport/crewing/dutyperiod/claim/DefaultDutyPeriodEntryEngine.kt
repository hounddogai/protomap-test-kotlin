package com.acme.transport.crewing.dutyperiod.claim

/** One step of the default duty period entry calculation. */
interface DefaultDutyPeriodEntryModifier {
    fun describe(): String
    fun adjust(item: DefaultDutyPeriodEntry, value: Double): Double
}

data class PercentDefaultDutyPeriodEntryAdjuster(var percent: Double) : DefaultDutyPeriodEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDutyPeriodEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultDutyPeriodEntryRevisionOffsetAdjuster(var weight: Double) : DefaultDutyPeriodEntryModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: DefaultDutyPeriodEntry, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class CapDefaultDutyPeriodEntryAdjuster(var cap: Double) : DefaultDutyPeriodEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDutyPeriodEntry, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultDutyPeriodEntryAdjuster(var minimum: Double) : DefaultDutyPeriodEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDutyPeriodEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default duty period entry steps in order and keeps a trace of each. */
data class DefaultDutyPeriodEntryEngine(
    var steps: MutableList<DefaultDutyPeriodEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultDutyPeriodEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDutyPeriodEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDutyPeriodEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultDutyPeriodEntryEngine(): DefaultDutyPeriodEntryEngine {
    return DefaultDutyPeriodEntryEngine(
        steps = mutableListOf<DefaultDutyPeriodEntryModifier>(
            PercentDefaultDutyPeriodEntryAdjuster(-11.1),
            DefaultDutyPeriodEntryRevisionOffsetAdjuster(0.28),
            CapDefaultDutyPeriodEntryAdjuster(42149.0),
            MinimumDefaultDutyPeriodEntryAdjuster(33.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 79134.7,
    )
}
