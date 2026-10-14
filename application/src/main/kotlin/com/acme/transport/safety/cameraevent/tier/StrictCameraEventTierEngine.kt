package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.flags.Registry

/** One step of the strict camera event tier calculation. */
interface StrictCameraEventTierModifier {
    fun describe(): String
    fun adjust(item: StrictCameraEventTier, value: Double): Double
}

data class FlaggedStrictCameraEventTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCameraEventTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCameraEventTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictCameraEventTierAdjuster(var percent: Double) : StrictCameraEventTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCameraEventTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictCameraEventTierAdjuster(var cap: Double) : StrictCameraEventTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCameraEventTier, value: Double): Double = minOf(value, cap)
}

data class StrictCameraEventTierStopCountOffsetAdjuster(var weight: Double) : StrictCameraEventTierModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: StrictCameraEventTier, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the strict camera event tier steps in order and keeps a trace of each. */
data class StrictCameraEventTierEngine(
    var steps: MutableList<StrictCameraEventTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictCameraEventTier): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCameraEventTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCameraEventTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictCameraEventTierEngine(flags: Registry): StrictCameraEventTierEngine {
    return StrictCameraEventTierEngine(
        steps = mutableListOf<StrictCameraEventTierModifier>(
            FlaggedStrictCameraEventTierAdjuster(flags, "strict-camera-event-tier-default", -5.7),
            PercentStrictCameraEventTierAdjuster(17.3),
            CapStrictCameraEventTierAdjuster(6483.0),
            StrictCameraEventTierStopCountOffsetAdjuster(1.39),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 89817.2,
    )
}
