package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan record calculation. */
interface ServicePlanRecordFactor {
    fun describe(): String
    fun adjust(item: ServicePlanRecord, value: Double): Double
}

data class TieredServicePlanRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ServicePlanRecordDistanceKmOffsetAdjuster(var weight: Double) : ServicePlanRecordFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ServicePlanRecord, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedServicePlanRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServicePlanRecordAdjuster(var step: Double) : ServicePlanRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanRecord, value: Double): Double = roundTo(value, step)
}

data class RegionServicePlanRecordAdjuster(var factors: MutableMap<String, Double>) : ServicePlanRecordFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ServicePlanRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentServicePlanRecordAdjuster(var percent: Double) : ServicePlanRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service plan record steps in order and keeps a trace of each. */
data class ServicePlanRecordEngine(
    var steps: MutableList<ServicePlanRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanRecord): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanRecordEngine(flags: Registry): ServicePlanRecordEngine {
    return ServicePlanRecordEngine(
        steps = mutableListOf<ServicePlanRecordFactor>(
            TieredServicePlanRecordAdjuster(
                mutableListOf<Double>(3365.0, 6988.0, 9027.0),
                mutableListOf<Double>(1.12, 0.98, 1.18),
            ),
            ServicePlanRecordDistanceKmOffsetAdjuster(1.49),
            FlaggedServicePlanRecordAdjuster(flags, "service-plan-record-pending", 7.5),
            RoundingServicePlanRecordAdjuster(5.0),
            RegionServicePlanRecordAdjuster(mutableMapOf<String, Double>("north" to 0.88)),
            PercentServicePlanRecordAdjuster(-2.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 74192.1,
    )
}
