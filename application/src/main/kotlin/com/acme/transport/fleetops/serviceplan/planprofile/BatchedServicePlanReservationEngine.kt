package com.acme.transport.fleetops.serviceplan.planprofile

/** One step of the batched service plan reservation calculation. */
interface BatchedServicePlanReservationModifier {
    fun describe(): String
    fun adjust(item: BatchedServicePlanReservation, value: Double): Double
}

data class CapBatchedServicePlanReservationAdjuster(var cap: Double) : BatchedServicePlanReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedServicePlanReservation, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedServicePlanReservationAdjuster(
    var percent: Double,
) : BatchedServicePlanReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedServicePlanReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedServicePlanReservationAdjuster(
    var minimum: Double,
) : BatchedServicePlanReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedServicePlanReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched service plan reservation steps in order and keeps a trace of each. */
data class BatchedServicePlanReservationEngine(
    var steps: MutableList<BatchedServicePlanReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedServicePlanReservation): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedServicePlanReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedServicePlanReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedServicePlanReservationEngine(): BatchedServicePlanReservationEngine {
    return BatchedServicePlanReservationEngine(
        steps = mutableListOf<BatchedServicePlanReservationModifier>(
            CapBatchedServicePlanReservationAdjuster(22048.0),
            PercentBatchedServicePlanReservationAdjuster(-20.4),
            MinimumBatchedServicePlanReservationAdjuster(7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 81536.0,
    )
}
