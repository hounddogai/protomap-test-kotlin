package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the camera event estimate calculation. */
interface CameraEventEstimateModifier {
    fun describe(): String
    fun adjust(item: CameraEventEstimate, value: Double): Double
}

data class TieredCameraEventEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCameraEventEstimateAdjuster(var percent: Double) : CameraEventEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCameraEventEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameCameraEventEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventEstimateModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CameraEventEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCameraEventEstimateAdjuster(var minimum: Double) : CameraEventEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CameraEventEstimateQuantityOffsetAdjuster(var weight: Double) : CameraEventEstimateModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CameraEventEstimate, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the camera event estimate steps in order and keeps a trace of each. */
data class CameraEventEstimateEngine(
    var steps: MutableList<CameraEventEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventEstimateEngine(flags: Registry): CameraEventEstimateEngine {
    return CameraEventEstimateEngine(
        steps = mutableListOf<CameraEventEstimateModifier>(
            TieredCameraEventEstimateAdjuster(
                mutableListOf<Double>(6874.0, 7513.0, 7841.0),
                mutableListOf<Double>(0.98, 1.16, 0.87),
            ),
            PercentCameraEventEstimateAdjuster(10.5),
            FlaggedCameraEventEstimateAdjuster(flags, "camera-event-estimate-weighted", -7.9),
            NameCameraEventEstimateAdjuster(mutableMapOf<String, Double>("south" to 1.24)),
            MinimumCameraEventEstimateAdjuster(42.3),
            CameraEventEstimateQuantityOffsetAdjuster(0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 27941.9,
    )
}
