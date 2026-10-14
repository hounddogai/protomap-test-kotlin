package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event profile calculation. */
interface CameraEventProfileStep {
    fun describe(): String
    fun adjust(item: CameraEventProfile, value: Double): Double
}

data class CapCameraEventProfileAdjuster(var cap: Double) : CameraEventProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventProfile, value: Double): Double = minOf(value, cap)
}

data class CameraEventProfileDistanceKmOffsetAdjuster(var weight: Double) : CameraEventProfileStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: CameraEventProfile, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedCameraEventProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCameraEventProfileAdjuster(var factors: MutableMap<String, Double>) : CameraEventProfileStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CameraEventProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the camera event profile steps in order and keeps a trace of each. */
data class CameraEventProfileEngine(
    var steps: MutableList<CameraEventProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventProfile): Double {
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

    fun evaluateAll(items: MutableList<CameraEventProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventProfileEngine(flags: Registry): CameraEventProfileEngine {
    return CameraEventProfileEngine(
        steps = mutableListOf<CameraEventProfileStep>(
            CapCameraEventProfileAdjuster(45894.0),
            CameraEventProfileDistanceKmOffsetAdjuster(0.91),
            FlaggedCameraEventProfileAdjuster(flags, "camera-event-profile-primary", -4.5),
            TitleCameraEventProfileAdjuster(mutableMapOf<String, Double>("west" to 0.79)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 41203.5,
    )
}
