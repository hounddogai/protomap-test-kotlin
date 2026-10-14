package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event target calculation. */
interface CameraEventTargetAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventTarget, value: Double): Double
}

data class TieredCameraEventTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCameraEventTargetAdjuster(var minimum: Double) : CameraEventTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCameraEventTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CameraEventTargetOdometerOffsetAdjuster(var weight: Double) : CameraEventTargetAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: CameraEventTarget, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class CapCameraEventTargetAdjuster(var cap: Double) : CameraEventTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the camera event target steps in order and keeps a trace of each. */
data class CameraEventTargetEngine(
    var steps: MutableList<CameraEventTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventTarget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventTargetEngine(flags: Registry): CameraEventTargetEngine {
    return CameraEventTargetEngine(
        steps = mutableListOf<CameraEventTargetAdjuster>(
            TieredCameraEventTargetAdjuster(
                mutableListOf<Double>(675.0, 3842.0, 6792.0),
                mutableListOf<Double>(0.89, 1.03, 0.82),
            ),
            MinimumCameraEventTargetAdjuster(37.5),
            FlaggedCameraEventTargetAdjuster(flags, "camera-event-target-lenient", -3.5),
            CameraEventTargetOdometerOffsetAdjuster(1.31),
            CapCameraEventTargetAdjuster(4103.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 47113.0,
    )
}
