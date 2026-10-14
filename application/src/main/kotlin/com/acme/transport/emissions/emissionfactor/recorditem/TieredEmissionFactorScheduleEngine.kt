package com.acme.transport.emissions.emissionfactor.recorditem

/** One step of the tiered emission factor schedule calculation. */
interface TieredEmissionFactorScheduleStep {
    fun describe(): String
    fun adjust(item: TieredEmissionFactorSchedule, value: Double): Double
}

data class TieredEmissionFactorScheduleWeightOffsetAdjuster(
    var weight: Double,
) : TieredEmissionFactorScheduleStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: TieredEmissionFactorSchedule, value: Double): Double = value + item.weight * weight
}

data class UnitTieredEmissionFactorScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEmissionFactorScheduleStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: TieredEmissionFactorSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredEmissionFactorScheduleAdjuster(var minimum: Double) : TieredEmissionFactorScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredEmissionFactorSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered emission factor schedule steps in order and keeps a trace of each. */
data class TieredEmissionFactorScheduleEngine(
    var steps: MutableList<TieredEmissionFactorScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredEmissionFactorSchedule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredEmissionFactorScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredEmissionFactorSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredEmissionFactorScheduleEngine(): TieredEmissionFactorScheduleEngine {
    return TieredEmissionFactorScheduleEngine(
        steps = mutableListOf<TieredEmissionFactorScheduleStep>(
            TieredEmissionFactorScheduleWeightOffsetAdjuster(1.84),
            UnitTieredEmissionFactorScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 1.11, "retail" to 1.36, "online" to 0.95),
            ),
            MinimumTieredEmissionFactorScheduleAdjuster(23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 91334.3,
    )
}
