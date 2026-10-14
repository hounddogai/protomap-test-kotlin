package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability bundle calculation. */
interface AvailabilityBundleAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityBundle, value: Double): Double
}

data class RoundingAvailabilityBundleAdjuster(var step: Double) : AvailabilityBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityBundle, value: Double): Double = roundTo(value, step)
}

data class DepotAvailabilityBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityBundleAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AvailabilityBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityBundleAdjuster(var minimum: Double) : AvailabilityBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilityBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability bundle steps in order and keeps a trace of each. */
data class AvailabilityBundleEngine(
    var steps: MutableList<AvailabilityBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityBundle): Double {
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

    fun evaluateAll(items: MutableList<AvailabilityBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityBundleEngine(flags: Registry): AvailabilityBundleEngine {
    return AvailabilityBundleEngine(
        steps = mutableListOf<AvailabilityBundleAdjuster>(
            RoundingAvailabilityBundleAdjuster(1.0),
            DepotAvailabilityBundleAdjuster(mutableMapOf<String, Double>("south" to 1.12, "west" to 0.99)),
            MinimumAvailabilityBundleAdjuster(27.0),
            FlaggedAvailabilityBundleAdjuster(flags, "availability-bundle-regional", -8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 82043.9,
    )
}
