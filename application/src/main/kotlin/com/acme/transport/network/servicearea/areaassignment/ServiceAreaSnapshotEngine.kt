package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area snapshot calculation. */
interface ServiceAreaSnapshotStep {
    fun describe(): String
    fun adjust(item: ServiceAreaSnapshot, value: Double): Double
}

data class CapServiceAreaSnapshotAdjuster(var cap: Double) : ServiceAreaSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaSnapshot, value: Double): Double = minOf(value, cap)
}

data class ServiceAreaSnapshotCostOffsetAdjuster(var weight: Double) : ServiceAreaSnapshotStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ServiceAreaSnapshot, value: Double): Double = value + item.cost * weight
}

data class RoundingServiceAreaSnapshotAdjuster(var step: Double) : ServiceAreaSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaSnapshot, value: Double): Double = roundTo(value, step)
}

data class PercentServiceAreaSnapshotAdjuster(var percent: Double) : ServiceAreaSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServiceAreaSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service area snapshot steps in order and keeps a trace of each. */
data class ServiceAreaSnapshotEngine(
    var steps: MutableList<ServiceAreaSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServiceAreaSnapshot): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServiceAreaSnapshotEngine(flags: Registry): ServiceAreaSnapshotEngine {
    return ServiceAreaSnapshotEngine(
        steps = mutableListOf<ServiceAreaSnapshotStep>(
            CapServiceAreaSnapshotAdjuster(771.0),
            ServiceAreaSnapshotCostOffsetAdjuster(0.44),
            RoundingServiceAreaSnapshotAdjuster(5.0),
            PercentServiceAreaSnapshotAdjuster(18.0),
            FlaggedServiceAreaSnapshotAdjuster(flags, "service-area-snapshot-lenient", 6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 57001.8,
    )
}
