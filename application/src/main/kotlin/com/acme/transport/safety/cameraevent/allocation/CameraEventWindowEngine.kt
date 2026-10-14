package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event window calculation. */
interface CameraEventWindowModifier {
    fun describe(): String
    fun adjust(item: CameraEventWindow, value: Double): Double
}

data class FlaggedCameraEventWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCameraEventWindowAdjuster(var cap: Double) : CameraEventWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumCameraEventWindowAdjuster(var minimum: Double) : CameraEventWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentCameraEventWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventWindowModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: CameraEventWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the camera event window steps in order and keeps a trace of each. */
data class CameraEventWindowEngine(
    var steps: MutableList<CameraEventWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventWindow): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventWindowEngine(flags: Registry): CameraEventWindowEngine {
    return CameraEventWindowEngine(
        steps = mutableListOf<CameraEventWindowModifier>(
            FlaggedCameraEventWindowAdjuster(flags, "camera-event-window-pending", 3.6),
            CapCameraEventWindowAdjuster(37558.0),
            MinimumCameraEventWindowAdjuster(5.3),
            SegmentCameraEventWindowAdjuster(mutableMapOf<String, Double>("central" to 1.37)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 8044.9,
    )
}
