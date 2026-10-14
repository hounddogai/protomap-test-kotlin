package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal capacity agreement calculation. */
interface SeasonalCapacityAgreementStep {
    fun describe(): String
    fun adjust(item: SeasonalCapacityAgreement, value: Double): Double
}

data class NameSeasonalCapacityAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCapacityAgreementStep {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalCapacityAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalCapacityAgreementAdjuster(var cap: Double) : SeasonalCapacityAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCapacityAgreement, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalCapacityAgreementAdjuster(var step: Double) : SeasonalCapacityAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCapacityAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal capacity agreement steps in order and keeps a trace of each. */
data class SeasonalCapacityAgreementEngine(
    var steps: MutableList<SeasonalCapacityAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalCapacityAgreement): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCapacityAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCapacityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalCapacityAgreementEngine(): SeasonalCapacityAgreementEngine {
    return SeasonalCapacityAgreementEngine(
        steps = mutableListOf<SeasonalCapacityAgreementStep>(
            NameSeasonalCapacityAgreementAdjuster(
                mutableMapOf<String, Double>("west" to 1.02, "online" to 1.31, "east" to 0.73),
            ),
            CapSeasonalCapacityAgreementAdjuster(30078.0),
            RoundingSeasonalCapacityAgreementAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 57385.6,
    )
}
