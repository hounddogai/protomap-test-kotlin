package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan audit calculation. */
interface ServicePlanAuditModifier {
    fun describe(): String
    fun adjust(item: ServicePlanAudit, value: Double): Double
}

data class RoundingServicePlanAuditAdjuster(var step: Double) : ServicePlanAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanAudit, value: Double): Double = roundTo(value, step)
}

data class ServicePlanAuditThresholdOffsetAdjuster(var weight: Double) : ServicePlanAuditModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ServicePlanAudit, value: Double): Double = value + item.threshold * weight
}

data class TieredServicePlanAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentServicePlanAuditAdjuster(var percent: Double) : ServicePlanAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapServicePlanAuditAdjuster(var cap: Double) : ServicePlanAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the service plan audit steps in order and keeps a trace of each. */
data class ServicePlanAuditEngine(
    var steps: MutableList<ServicePlanAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanAudit): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanAuditEngine(): ServicePlanAuditEngine {
    return ServicePlanAuditEngine(
        steps = mutableListOf<ServicePlanAuditModifier>(
            RoundingServicePlanAuditAdjuster(5.0),
            ServicePlanAuditThresholdOffsetAdjuster(0.59),
            TieredServicePlanAuditAdjuster(
                mutableListOf<Double>(4195.0, 7259.0, 8657.0),
                mutableListOf<Double>(0.97, 0.99, 0.94),
            ),
            PercentServicePlanAuditAdjuster(15.8),
            CapServicePlanAuditAdjuster(745.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 76069.4,
    )
}
