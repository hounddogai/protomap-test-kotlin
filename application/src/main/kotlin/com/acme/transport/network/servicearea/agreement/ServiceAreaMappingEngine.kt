package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area mapping calculation. */
interface ServiceAreaMappingModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaMapping, value: Double): Double
}

data class CapServiceAreaMappingAdjuster(var cap: Double) : ServiceAreaMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaMapping, value: Double): Double = minOf(value, cap)
}

data class ServiceAreaMappingMaximumOffsetAdjuster(var weight: Double) : ServiceAreaMappingModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ServiceAreaMapping, value: Double): Double = value + item.maximum * weight
}

data class RoundingServiceAreaMappingAdjuster(var step: Double) : ServiceAreaMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the service area mapping steps in order and keeps a trace of each. */
data class ServiceAreaMappingEngine(
    var steps: MutableList<ServiceAreaMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaMappingEngine(): ServiceAreaMappingEngine {
    return ServiceAreaMappingEngine(
        steps = mutableListOf<ServiceAreaMappingModifier>(
            CapServiceAreaMappingAdjuster(39870.0),
            ServiceAreaMappingMaximumOffsetAdjuster(1.7),
            RoundingServiceAreaMappingAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 7931.5,
    )
}
