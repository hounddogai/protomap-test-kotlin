package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the camera event manifest calculation. */
interface CameraEventManifestModifier {
    fun describe(): String
    fun adjust(item: CameraEventManifest, value: Double): Double
}

data class PercentCameraEventManifestAdjuster(var percent: Double) : CameraEventManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCameraEventManifestAdjuster(var minimum: Double) : CameraEventManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCameraEventManifestAdjuster(var cap: Double) : CameraEventManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventManifest, value: Double): Double = minOf(value, cap)
}

data class TieredCameraEventManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCameraEventManifestAdjuster(var step: Double) : CameraEventManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the camera event manifest steps in order and keeps a trace of each. */
data class CameraEventManifestEngine(
    var steps: MutableList<CameraEventManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventManifestEngine(): CameraEventManifestEngine {
    return CameraEventManifestEngine(
        steps = mutableListOf<CameraEventManifestModifier>(
            PercentCameraEventManifestAdjuster(-6.9),
            MinimumCameraEventManifestAdjuster(47.5),
            CapCameraEventManifestAdjuster(35805.0),
            TieredCameraEventManifestAdjuster(
                mutableListOf<Double>(1774.0, 4283.0),
                mutableListOf<Double>(1.14, 1.07),
            ),
            RoundingCameraEventManifestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 82609.6,
    )
}
