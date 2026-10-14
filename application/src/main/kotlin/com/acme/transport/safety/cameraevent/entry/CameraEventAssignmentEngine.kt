package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event assignment calculation. */
interface CameraEventAssignmentStep {
    fun describe(): String
    fun adjust(item: CameraEventAssignment, value: Double): Double
}

data class FlaggedCameraEventAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCameraEventAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCameraEventAssignmentAdjuster(var step: Double) : CameraEventAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumCameraEventAssignmentAdjuster(var minimum: Double) : CameraEventAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the camera event assignment steps in order and keeps a trace of each. */
data class CameraEventAssignmentEngine(
    var steps: MutableList<CameraEventAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventAssignment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventAssignmentEngine(flags: Registry): CameraEventAssignmentEngine {
    return CameraEventAssignmentEngine(
        steps = mutableListOf<CameraEventAssignmentStep>(
            FlaggedCameraEventAssignmentAdjuster(flags, "camera-event-assignment-regional", -4.5),
            TieredCameraEventAssignmentAdjuster(
                mutableListOf<Double>(1179.0, 8990.0),
                mutableListOf<Double>(0.8, 0.95),
            ),
            RoundingCameraEventAssignmentAdjuster(0.05),
            MinimumCameraEventAssignmentAdjuster(17.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 65147.0,
    )
}
