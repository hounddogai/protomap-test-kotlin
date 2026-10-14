package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan agreement calculation. */
interface ServicePlanAgreementAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanAgreement, value: Double): Double
}

data class MinimumServicePlanAgreementAdjuster(var minimum: Double) : ServicePlanAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitServicePlanAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ServicePlanAgreementAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: ServicePlanAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingServicePlanAgreementAdjuster(var step: Double) : ServicePlanAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredServicePlanAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapServicePlanAgreementAdjuster(var cap: Double) : ServicePlanAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the service plan agreement steps in order and keeps a trace of each. */
data class ServicePlanAgreementEngine(
    var steps: MutableList<ServicePlanAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanAgreement): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanAgreementEngine(): ServicePlanAgreementEngine {
    return ServicePlanAgreementEngine(
        steps = mutableListOf<ServicePlanAgreementAdjuster>(
            MinimumServicePlanAgreementAdjuster(29.8),
            UnitServicePlanAgreementAdjuster(
                mutableMapOf<String, Double>("online" to 0.94, "central" to 1.09, "east" to 1.39),
            ),
            RoundingServicePlanAgreementAdjuster(0.5),
            TieredServicePlanAgreementAdjuster(
                mutableListOf<Double>(663.0, 2049.0, 8655.0),
                mutableListOf<Double>(1.13, 0.91, 0.81),
            ),
            CapServicePlanAgreementAdjuster(7687.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 3038.4,
    )
}
