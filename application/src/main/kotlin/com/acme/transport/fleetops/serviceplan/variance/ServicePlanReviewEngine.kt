package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan review calculation. */
interface ServicePlanReviewAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanReview, value: Double): Double
}

data class CapServicePlanReviewAdjuster(var cap: Double) : ServicePlanReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanReview, value: Double): Double = minOf(value, cap)
}

data class ServicePlanReviewCapacityOffsetAdjuster(var weight: Double) : ServicePlanReviewAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ServicePlanReview, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class PercentServicePlanReviewAdjuster(var percent: Double) : ServicePlanReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingServicePlanReviewAdjuster(var step: Double) : ServicePlanReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanReview, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan review steps in order and keeps a trace of each. */
data class ServicePlanReviewEngine(
    var steps: MutableList<ServicePlanReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanReview): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanReviewEngine(): ServicePlanReviewEngine {
    return ServicePlanReviewEngine(
        steps = mutableListOf<ServicePlanReviewAdjuster>(
            CapServicePlanReviewAdjuster(46467.0),
            ServicePlanReviewCapacityOffsetAdjuster(1.19),
            PercentServicePlanReviewAdjuster(14.1),
            RoundingServicePlanReviewAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 75876.7,
    )
}
