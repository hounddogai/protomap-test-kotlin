package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan reservation calculation. */
interface ServicePlanReservationStep {
    fun describe(): String
    fun adjust(item: ServicePlanReservation, value: Double): Double
}

data class ServicePlanReservationMarginOffsetAdjuster(var weight: Double) : ServicePlanReservationStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ServicePlanReservation, value: Double): Double = value + item.margin * weight
}

data class TieredServicePlanReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingServicePlanReservationAdjuster(var step: Double) : ServicePlanReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan reservation steps in order and keeps a trace of each. */
data class ServicePlanReservationEngine(
    var steps: MutableList<ServicePlanReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanReservation): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanReservationEngine(): ServicePlanReservationEngine {
    return ServicePlanReservationEngine(
        steps = mutableListOf<ServicePlanReservationStep>(
            ServicePlanReservationMarginOffsetAdjuster(1.32),
            TieredServicePlanReservationAdjuster(
                mutableListOf<Double>(753.0, 4420.0, 6419.0, 8379.0),
                mutableListOf<Double>(0.86, 1.15, 1.13, 0.96),
            ),
            RoundingServicePlanReservationAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 13800.6,
    )
}
