package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the global capacity agreement calculation. */
interface GlobalCapacityAgreementFactor {
    fun describe(): String
    fun adjust(item: GlobalCapacityAgreement, value: Double): Double
}

data class RoundingGlobalCapacityAgreementAdjuster(var step: Double) : GlobalCapacityAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCapacityAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalCapacityAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCapacityAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCapacityAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotGlobalCapacityAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCapacityAgreementFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalCapacityAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global capacity agreement steps in order and keeps a trace of each. */
data class GlobalCapacityAgreementEngine(
    var steps: MutableList<GlobalCapacityAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCapacityAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCapacityAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCapacityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCapacityAgreementEngine(): GlobalCapacityAgreementEngine {
    return GlobalCapacityAgreementEngine(
        steps = mutableListOf<GlobalCapacityAgreementFactor>(
            RoundingGlobalCapacityAgreementAdjuster(5.0),
            TieredGlobalCapacityAgreementAdjuster(
                mutableListOf<Double>(407.0, 4634.0, 4675.0),
                mutableListOf<Double>(1.04, 1.05, 0.85),
            ),
            DepotGlobalCapacityAgreementAdjuster(
                mutableMapOf<String, Double>("east" to 1.01, "west" to 1.35, "south" to 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 87147.0,
    )
}
