package com.acme.transport.safety.cameraevent.eventbundle

/** One step of the camera event threshold calculation. */
interface CameraEventThresholdAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventThreshold, value: Double): Double
}

data class CapCameraEventThresholdAdjuster(var cap: Double) : CameraEventThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventThreshold, value: Double): Double = minOf(value, cap)
}

data class CountryCameraEventThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventThresholdAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: CameraEventThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCameraEventThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the camera event threshold steps in order and keeps a trace of each. */
data class CameraEventThresholdEngine(
    var steps: MutableList<CameraEventThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CameraEventThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventThresholdAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCameraEventThresholdEngine(): CameraEventThresholdEngine {
    return CameraEventThresholdEngine(
        steps = mutableListOf<CameraEventThresholdAdjuster>(
            CapCameraEventThresholdAdjuster(29493.0),
            CountryCameraEventThresholdAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "east" to 1.21, "online" to 1.19),
            ),
            TieredCameraEventThresholdAdjuster(
                mutableListOf<Double>(1273.0, 6949.0),
                mutableListOf<Double>(0.88, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 68227.4,
    )
}
