package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict camera event cycle calculation. */
interface StrictCameraEventCycleAdjuster {
    fun describe(): String
    fun adjust(item: StrictCameraEventCycle, value: Double): Double
}

data class RoundingStrictCameraEventCycleAdjuster(var step: Double) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double = roundTo(value, step)
}

data class StrictCameraEventCycleStartsAtOffsetAdjuster(var weight: Double) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapStrictCameraEventCycleAdjuster(var cap: Double) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictCameraEventCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictCameraEventCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictCameraEventCycleAdjuster(var minimum: Double) : StrictCameraEventCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCameraEventCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict camera event cycle steps in order and keeps a trace of each. */
data class StrictCameraEventCycleEngine(
    var steps: MutableList<StrictCameraEventCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCameraEventCycle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCameraEventCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCameraEventCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCameraEventCycleEngine(flags: Registry): StrictCameraEventCycleEngine {
    return StrictCameraEventCycleEngine(
        steps = mutableListOf<StrictCameraEventCycleAdjuster>(
            RoundingStrictCameraEventCycleAdjuster(1.0),
            StrictCameraEventCycleStartsAtOffsetAdjuster(0.5),
            CapStrictCameraEventCycleAdjuster(19115.0),
            FlaggedStrictCameraEventCycleAdjuster(flags, "strict-camera-event-cycle-pending", 8.6),
            TieredStrictCameraEventCycleAdjuster(
                mutableListOf<Double>(2882.0, 6632.0),
                mutableListOf<Double>(1.19, 1.0),
            ),
            MinimumStrictCameraEventCycleAdjuster(28.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 40418.4,
    )
}
