package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.Registry

/** One step of the capped cutoff schedule calculation. */
interface CappedCutoffScheduleFactor {
    fun describe(): String
    fun adjust(item: CappedCutoffSchedule, value: Double): Double
}

data class FlaggedCappedCutoffScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCutoffScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCutoffSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedCutoffScheduleAdjuster(var percent: Double) : CappedCutoffScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCutoffSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedCutoffScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCutoffScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCutoffSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameCappedCutoffScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCutoffScheduleFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedCutoffSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped cutoff schedule steps in order and keeps a trace of each. */
data class CappedCutoffScheduleEngine(
    var steps: MutableList<CappedCutoffScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedCutoffSchedule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCutoffScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCutoffSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedCutoffScheduleEngine(flags: Registry): CappedCutoffScheduleEngine {
    return CappedCutoffScheduleEngine(
        steps = mutableListOf<CappedCutoffScheduleFactor>(
            FlaggedCappedCutoffScheduleAdjuster(flags, "capped-cutoff-schedule-rolling", -1.3),
            PercentCappedCutoffScheduleAdjuster(14.7),
            TieredCappedCutoffScheduleAdjuster(
                mutableListOf<Double>(2939.0, 6621.0, 9925.0),
                mutableListOf<Double>(1.17, 1.05, 0.87),
            ),
            NameCappedCutoffScheduleAdjuster(mutableMapOf<String, Double>("retail" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 38850.6,
    )
}
