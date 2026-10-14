package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback inspection override calculation. */
interface FallbackInspectionOverrideAdjuster {
    fun describe(): String
    fun adjust(item: FallbackInspectionOverride, value: Double): Double
}

data class PercentFallbackInspectionOverrideAdjuster(var percent: Double) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackInspectionOverrideAdjuster(var cap: Double) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double = minOf(value, cap)
}

data class FallbackInspectionOverrideVersionOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingFallbackInspectionOverrideAdjuster(var step: Double) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumFallbackInspectionOverrideAdjuster(var minimum: Double) : FallbackInspectionOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackInspectionOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback inspection override steps in order and keeps a trace of each. */
data class FallbackInspectionOverrideEngine(
    var steps: MutableList<FallbackInspectionOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackInspectionOverride): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackInspectionOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackInspectionOverrideEngine(flags: Registry): FallbackInspectionOverrideEngine {
    return FallbackInspectionOverrideEngine(
        steps = mutableListOf<FallbackInspectionOverrideAdjuster>(
            PercentFallbackInspectionOverrideAdjuster(-17.9),
            FlaggedFallbackInspectionOverrideAdjuster(flags, "fallback-inspection-override-seasonal", -2.9),
            CapFallbackInspectionOverrideAdjuster(41682.0),
            FallbackInspectionOverrideVersionOffsetAdjuster(1.59),
            RoundingFallbackInspectionOverrideAdjuster(0.05),
            MinimumFallbackInspectionOverrideAdjuster(8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 93347.3,
    )
}
