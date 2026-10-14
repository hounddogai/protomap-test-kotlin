package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event baseline calculation. */
interface CameraEventBaselineStep {
    fun describe(): String
    fun adjust(item: CameraEventBaseline, value: Double): Double
}

data class MinimumCameraEventBaselineAdjuster(var minimum: Double) : CameraEventBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCameraEventBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCameraEventBaselineAdjuster(var percent: Double) : CameraEventBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCameraEventBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the camera event baseline steps in order and keeps a trace of each. */
data class CameraEventBaselineEngine(
    var steps: MutableList<CameraEventBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventBaseline): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventBaselineEngine(flags: Registry): CameraEventBaselineEngine {
    return CameraEventBaselineEngine(
        steps = mutableListOf<CameraEventBaselineStep>(
            MinimumCameraEventBaselineAdjuster(20.5),
            TieredCameraEventBaselineAdjuster(
                mutableListOf<Double>(416.0, 1880.0, 8949.0),
                mutableListOf<Double>(1.02, 1.01, 0.92),
            ),
            PercentCameraEventBaselineAdjuster(-14.6),
            FlaggedCameraEventBaselineAdjuster(flags, "camera-event-baseline-weighted", 0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 73904.8,
    )
}
