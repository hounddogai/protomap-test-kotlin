package com.acme.transport.safety.cameraevent.bundle

/** One step of the camera event bundle calculation. */
interface CameraEventBundleModifier {
    fun describe(): String
    fun adjust(item: CameraEventBundle, value: Double): Double
}

data class CapCameraEventBundleAdjuster(var cap: Double) : CameraEventBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumCameraEventBundleAdjuster(var minimum: Double) : CameraEventBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCameraEventBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCameraEventBundleAdjuster(var percent: Double) : CameraEventBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the camera event bundle steps in order and keeps a trace of each. */
data class CameraEventBundleEngine(
    var steps: MutableList<CameraEventBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CameraEventBundle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCameraEventBundleEngine(): CameraEventBundleEngine {
    return CameraEventBundleEngine(
        steps = mutableListOf<CameraEventBundleModifier>(
            CapCameraEventBundleAdjuster(13579.0),
            MinimumCameraEventBundleAdjuster(39.4),
            TieredCameraEventBundleAdjuster(
                mutableListOf<Double>(763.0, 1017.0, 3618.0, 7923.0),
                mutableListOf<Double>(1.16, 1.16, 1.03, 1.05),
            ),
            PercentCameraEventBundleAdjuster(5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 49412.6,
    )
}
