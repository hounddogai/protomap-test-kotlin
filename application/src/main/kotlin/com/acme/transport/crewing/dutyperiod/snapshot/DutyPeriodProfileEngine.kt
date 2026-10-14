package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the duty period profile calculation. */
interface DutyPeriodProfileStep {
    fun describe(): String
    fun adjust(item: DutyPeriodProfile, value: Double): Double
}

data class RoundingDutyPeriodProfileAdjuster(var step: Double) : DutyPeriodProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumDutyPeriodProfileAdjuster(var minimum: Double) : DutyPeriodProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDutyPeriodProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the duty period profile steps in order and keeps a trace of each. */
data class DutyPeriodProfileEngine(
    var steps: MutableList<DutyPeriodProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodProfileEngine(): DutyPeriodProfileEngine {
    return DutyPeriodProfileEngine(
        steps = mutableListOf<DutyPeriodProfileStep>(
            RoundingDutyPeriodProfileAdjuster(0.05),
            MinimumDutyPeriodProfileAdjuster(23.6),
            TieredDutyPeriodProfileAdjuster(
                mutableListOf<Double>(1734.0, 6040.0, 6256.0),
                mutableListOf<Double>(1.04, 0.88, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 83314.6,
    )
}
