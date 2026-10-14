package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event tier calculation. */
interface CameraEventTierFactor {
    fun describe(): String
    fun adjust(item: CameraEventTier, value: Double): Double
}

data class RoundingCameraEventTierAdjuster(var step: Double) : CameraEventTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventTier, value: Double): Double = roundTo(value, step)
}

data class TieredCameraEventTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCameraEventTierAdjuster(var minimum: Double) : CameraEventTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCameraEventTierAdjuster(var percent: Double) : CameraEventTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the camera event tier steps in order and keeps a trace of each. */
data class CameraEventTierEngine(
    var steps: MutableList<CameraEventTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CameraEventTier): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCameraEventTierEngine(): CameraEventTierEngine {
    return CameraEventTierEngine(
        steps = mutableListOf<CameraEventTierFactor>(
            RoundingCameraEventTierAdjuster(0.25),
            TieredCameraEventTierAdjuster(
                mutableListOf<Double>(1326.0, 6973.0, 7757.0),
                mutableListOf<Double>(1.18, 0.9, 0.84),
            ),
            MinimumCameraEventTierAdjuster(29.2),
            PercentCameraEventTierAdjuster(4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 93851.8,
    )
}
