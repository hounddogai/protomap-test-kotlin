package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling camera event snapshot calculation. */
interface RollingCameraEventSnapshotStep {
    fun describe(): String
    fun adjust(item: RollingCameraEventSnapshot, value: Double): Double
}

data class RoundingRollingCameraEventSnapshotAdjuster(var step: Double) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double = roundTo(value, step)
}

data class RegionRollingCameraEventSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "by region"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingCameraEventSnapshotAdjuster(var minimum: Double) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingCameraEventSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingCameraEventSnapshotAdjuster(var cap: Double) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingCameraEventSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCameraEventSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCameraEventSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling camera event snapshot steps in order and keeps a trace of each. */
data class RollingCameraEventSnapshotEngine(
    var steps: MutableList<RollingCameraEventSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingCameraEventSnapshot): Double {
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

    fun evaluateAll(items: MutableList<RollingCameraEventSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingCameraEventSnapshotEngine(flags: Registry): RollingCameraEventSnapshotEngine {
    return RollingCameraEventSnapshotEngine(
        steps = mutableListOf<RollingCameraEventSnapshotStep>(
            RoundingRollingCameraEventSnapshotAdjuster(5.0),
            RegionRollingCameraEventSnapshotAdjuster(
                mutableMapOf<String, Double>("online" to 1.2, "south" to 0.79, "central" to 1.34),
            ),
            MinimumRollingCameraEventSnapshotAdjuster(15.8),
            TieredRollingCameraEventSnapshotAdjuster(
                mutableListOf<Double>(4156.0, 4642.0, 5294.0, 7726.0),
                mutableListOf<Double>(1.05, 1.12, 0.81, 1.02),
            ),
            CapRollingCameraEventSnapshotAdjuster(32564.0),
            FlaggedRollingCameraEventSnapshotAdjuster(flags, "rolling-camera-event-snapshot-adaptive", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 12485.5,
    )
}
