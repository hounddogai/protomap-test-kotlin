package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the camera event template calculation. */
interface CameraEventTemplateAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventTemplate, value: Double): Double
}

data class CameraEventTemplateExpiresAtOffsetAdjuster(var weight: Double) : CameraEventTemplateAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CameraEventTemplate, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumCameraEventTemplateAdjuster(var minimum: Double) : CameraEventTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryCameraEventTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventTemplateAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: CameraEventTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCameraEventTemplateAdjuster(var cap: Double) : CameraEventTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventTemplate, value: Double): Double = minOf(value, cap)
}

data class FlaggedCameraEventTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCameraEventTemplateAdjuster(var step: Double) : CameraEventTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventTemplate, value: Double): Double = roundTo(value, step)
}

/** Runs the camera event template steps in order and keeps a trace of each. */
data class CameraEventTemplateEngine(
    var steps: MutableList<CameraEventTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventTemplate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventTemplateEngine(flags: Registry): CameraEventTemplateEngine {
    return CameraEventTemplateEngine(
        steps = mutableListOf<CameraEventTemplateAdjuster>(
            CameraEventTemplateExpiresAtOffsetAdjuster(1.62),
            MinimumCameraEventTemplateAdjuster(22.1),
            CountryCameraEventTemplateAdjuster(mutableMapOf<String, Double>("south" to 1.31)),
            CapCameraEventTemplateAdjuster(32581.0),
            FlaggedCameraEventTemplateAdjuster(flags, "camera-event-template-tiered", 1.3),
            RoundingCameraEventTemplateAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 85148.1,
    )
}
