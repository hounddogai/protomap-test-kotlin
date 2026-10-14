package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area override calculation. */
interface ServiceAreaOverrideAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaOverride, value: Double): Double
}

data class SegmentServiceAreaOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServiceAreaOverrideAdjuster(var minimum: Double) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ServiceAreaOverrideGraceDaysOffsetAdjuster(var weight: Double) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class FlaggedServiceAreaOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServiceAreaOverrideAdjuster(var step: Double) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double = roundTo(value, step)
}

data class PercentServiceAreaOverrideAdjuster(var percent: Double) : ServiceAreaOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service area override steps in order and keeps a trace of each. */
data class ServiceAreaOverrideEngine(
    var steps: MutableList<ServiceAreaOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServiceAreaOverride): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServiceAreaOverrideEngine(flags: Registry): ServiceAreaOverrideEngine {
    return ServiceAreaOverrideEngine(
        steps = mutableListOf<ServiceAreaOverrideAdjuster>(
            SegmentServiceAreaOverrideAdjuster(mutableMapOf<String, Double>("west" to 1.28)),
            MinimumServiceAreaOverrideAdjuster(10.0),
            ServiceAreaOverrideGraceDaysOffsetAdjuster(0.08),
            FlaggedServiceAreaOverrideAdjuster(flags, "service-area-override-batched", 6.1),
            RoundingServiceAreaOverrideAdjuster(0.05),
            PercentServiceAreaOverrideAdjuster(-13.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 64089.1,
    )
}
