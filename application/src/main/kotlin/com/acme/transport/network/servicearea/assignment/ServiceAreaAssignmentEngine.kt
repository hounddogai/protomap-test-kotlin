package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area assignment calculation. */
interface ServiceAreaAssignmentStep {
    fun describe(): String
    fun adjust(item: ServiceAreaAssignment, value: Double): Double
}

data class CapServiceAreaAssignmentAdjuster(var cap: Double) : ServiceAreaAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingServiceAreaAssignmentAdjuster(var step: Double) : ServiceAreaAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaAssignment, value: Double): Double = roundTo(value, step)
}

data class ServiceAreaAssignmentAttemptsOffsetAdjuster(var weight: Double) : ServiceAreaAssignmentStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ServiceAreaAssignment, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the service area assignment steps in order and keeps a trace of each. */
data class ServiceAreaAssignmentEngine(
    var steps: MutableList<ServiceAreaAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaAssignmentEngine(): ServiceAreaAssignmentEngine {
    return ServiceAreaAssignmentEngine(
        steps = mutableListOf<ServiceAreaAssignmentStep>(
            CapServiceAreaAssignmentAdjuster(45220.0),
            RoundingServiceAreaAssignmentAdjuster(0.5),
            ServiceAreaAssignmentAttemptsOffsetAdjuster(0.82),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 11177.4,
    )
}
