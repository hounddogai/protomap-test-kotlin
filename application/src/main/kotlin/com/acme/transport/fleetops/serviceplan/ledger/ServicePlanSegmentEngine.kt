package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan segment calculation. */
interface ServicePlanSegmentStep {
    fun describe(): String
    fun adjust(item: ServicePlanSegment, value: Double): Double
}

data class CapServicePlanSegmentAdjuster(var cap: Double) : ServicePlanSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanSegment, value: Double): Double = minOf(value, cap)
}

data class TitleServicePlanSegmentAdjuster(var factors: MutableMap<String, Double>) : ServicePlanSegmentStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ServicePlanSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ServicePlanSegmentCostOffsetAdjuster(var weight: Double) : ServicePlanSegmentStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ServicePlanSegment, value: Double): Double = value + item.cost * weight
}

data class TieredServicePlanSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingServicePlanSegmentAdjuster(var step: Double) : ServicePlanSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan segment steps in order and keeps a trace of each. */
data class ServicePlanSegmentEngine(
    var steps: MutableList<ServicePlanSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanSegment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanSegmentEngine(): ServicePlanSegmentEngine {
    return ServicePlanSegmentEngine(
        steps = mutableListOf<ServicePlanSegmentStep>(
            CapServicePlanSegmentAdjuster(26370.0),
            TitleServicePlanSegmentAdjuster(mutableMapOf<String, Double>("north" to 1.02)),
            ServicePlanSegmentCostOffsetAdjuster(1.39),
            TieredServicePlanSegmentAdjuster(
                mutableListOf<Double>(3727.0, 4754.0, 7342.0),
                mutableListOf<Double>(0.91, 1.2, 0.84),
            ),
            RoundingServicePlanSegmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 62806.6,
    )
}
