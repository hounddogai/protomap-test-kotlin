package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event slot calculation. */
interface CameraEventSlotFactor {
    fun describe(): String
    fun adjust(item: CameraEventSlot, value: Double): Double
}

data class PercentCameraEventSlotAdjuster(var percent: Double) : CameraEventSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCameraEventSlotAdjuster(var minimum: Double) : CameraEventSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CameraEventSlotEtaMinutesOffsetAdjuster(var weight: Double) : CameraEventSlotFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CameraEventSlot, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class TieredCameraEventSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCameraEventSlotAdjuster(var step: Double) : CameraEventSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the camera event slot steps in order and keeps a trace of each. */
data class CameraEventSlotEngine(
    var steps: MutableList<CameraEventSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventSlot): Double {
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

    fun evaluateAll(items: MutableList<CameraEventSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventSlotEngine(): CameraEventSlotEngine {
    return CameraEventSlotEngine(
        steps = mutableListOf<CameraEventSlotFactor>(
            PercentCameraEventSlotAdjuster(-9.4),
            MinimumCameraEventSlotAdjuster(29.4),
            CameraEventSlotEtaMinutesOffsetAdjuster(0.13),
            TieredCameraEventSlotAdjuster(
                mutableListOf<Double>(585.0, 1031.0, 2494.0, 5719.0),
                mutableListOf<Double>(1.15, 0.85, 1.18, 0.88),
            ),
            RoundingCameraEventSlotAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 69532.3,
    )
}
