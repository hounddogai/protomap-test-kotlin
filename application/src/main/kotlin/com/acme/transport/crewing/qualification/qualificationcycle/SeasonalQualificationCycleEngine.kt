package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal qualification cycle calculation. */
interface SeasonalQualificationCycleStep {
    fun describe(): String
    fun adjust(item: SeasonalQualificationCycle, value: Double): Double
}

data class TieredSeasonalQualificationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalQualificationCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalQualificationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalQualificationCycleAdjuster(var cap: Double) : SeasonalQualificationCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalQualificationCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalQualificationCycleAdjuster(var step: Double) : SeasonalQualificationCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalQualificationCycle, value: Double): Double = roundTo(value, step)
}

data class TitleSeasonalQualificationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalQualificationCycleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalQualificationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal qualification cycle steps in order and keeps a trace of each. */
data class SeasonalQualificationCycleEngine(
    var steps: MutableList<SeasonalQualificationCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalQualificationCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalQualificationCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalQualificationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalQualificationCycleEngine(): SeasonalQualificationCycleEngine {
    return SeasonalQualificationCycleEngine(
        steps = mutableListOf<SeasonalQualificationCycleStep>(
            TieredSeasonalQualificationCycleAdjuster(
                mutableListOf<Double>(2410.0, 5376.0, 7817.0, 8469.0),
                mutableListOf<Double>(0.8, 1.04, 1.18, 0.96),
            ),
            CapSeasonalQualificationCycleAdjuster(15222.0),
            RoundingSeasonalQualificationCycleAdjuster(5.0),
            TitleSeasonalQualificationCycleAdjuster(mutableMapOf<String, Double>("west" to 0.75, "east" to 1.1)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 42621.0,
    )
}
