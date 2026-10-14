package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event entry calculation. */
interface CameraEventEntryAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventEntry, value: Double): Double
}

data class FlaggedCameraEventEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCameraEventEntryAdjuster(var percent: Double) : CameraEventEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCameraEventEntryAdjuster(var step: Double) : CameraEventEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventEntry, value: Double): Double = roundTo(value, step)
}

data class TieredCameraEventEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCameraEventEntryAdjuster(var cap: Double) : CameraEventEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the camera event entry steps in order and keeps a trace of each. */
data class CameraEventEntryEngine(
    var steps: MutableList<CameraEventEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventEntry): Double {
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

    fun evaluateAll(items: MutableList<CameraEventEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventEntryEngine(flags: Registry): CameraEventEntryEngine {
    return CameraEventEntryEngine(
        steps = mutableListOf<CameraEventEntryAdjuster>(
            FlaggedCameraEventEntryAdjuster(flags, "camera-event-entry-fallback", 7.7),
            PercentCameraEventEntryAdjuster(20.0),
            RoundingCameraEventEntryAdjuster(0.05),
            TieredCameraEventEntryAdjuster(
                mutableListOf<Double>(431.0, 6720.0, 8451.0, 9434.0),
                mutableListOf<Double>(0.9, 1.16, 1.02, 1.07),
            ),
            CapCameraEventEntryAdjuster(27320.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 86920.9,
    )
}
