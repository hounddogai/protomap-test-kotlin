package com.acme.transport.emissions.emissionfactor.factorschedule

/** One step of the regional emission factor schedule calculation. */
interface RegionalEmissionFactorScheduleFactor {
    fun describe(): String
    fun adjust(item: RegionalEmissionFactorSchedule, value: Double): Double
}

data class RegionalEmissionFactorScheduleAttemptsOffsetAdjuster(
    var weight: Double,
) : RegionalEmissionFactorScheduleFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RegionalEmissionFactorSchedule, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class PercentRegionalEmissionFactorScheduleAdjuster(
    var percent: Double,
) : RegionalEmissionFactorScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalEmissionFactorSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleRegionalEmissionFactorScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalEmissionFactorScheduleFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalEmissionFactorSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional emission factor schedule steps in order and keeps a trace of each. */
data class RegionalEmissionFactorScheduleEngine(
    var steps: MutableList<RegionalEmissionFactorScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalEmissionFactorSchedule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalEmissionFactorScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalEmissionFactorSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalEmissionFactorScheduleEngine(): RegionalEmissionFactorScheduleEngine {
    return RegionalEmissionFactorScheduleEngine(
        steps = mutableListOf<RegionalEmissionFactorScheduleFactor>(
            RegionalEmissionFactorScheduleAttemptsOffsetAdjuster(0.2),
            PercentRegionalEmissionFactorScheduleAdjuster(9.3),
            TitleRegionalEmissionFactorScheduleAdjuster(mutableMapOf<String, Double>("west" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 33279.7,
    )
}
