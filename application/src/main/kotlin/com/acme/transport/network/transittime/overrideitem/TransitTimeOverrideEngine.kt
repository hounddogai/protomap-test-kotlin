package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time override calculation. */
interface TransitTimeOverrideStep {
    fun describe(): String
    fun adjust(item: TransitTimeOverride, value: Double): Double
}

data class FlaggedTransitTimeOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryTransitTimeOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeOverrideStep {
    override fun describe(): String = "by category"

    override fun adjust(item: TransitTimeOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTransitTimeOverrideAdjuster(var step: Double) : TransitTimeOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumTransitTimeOverrideAdjuster(var minimum: Double) : TransitTimeOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time override steps in order and keeps a trace of each. */
data class TransitTimeOverrideEngine(
    var steps: MutableList<TransitTimeOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeOverride): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeOverrideEngine(flags: Registry): TransitTimeOverrideEngine {
    return TransitTimeOverrideEngine(
        steps = mutableListOf<TransitTimeOverrideStep>(
            FlaggedTransitTimeOverrideAdjuster(flags, "transit-time-override-tiered", -3.3),
            CategoryTransitTimeOverrideAdjuster(
                mutableMapOf<String, Double>("retail" to 1.21, "online" to 0.91, "east" to 1.16),
            ),
            RoundingTransitTimeOverrideAdjuster(0.25),
            MinimumTransitTimeOverrideAdjuster(6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 15399.7,
    )
}
