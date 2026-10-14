package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the camera event request calculation. */
interface CameraEventRequestStep {
    fun describe(): String
    fun adjust(item: CameraEventRequest, value: Double): Double
}

data class TieredCameraEventRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CameraEventRequestAmountOffsetAdjuster(var weight: Double) : CameraEventRequestStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CameraEventRequest, value: Double): Double = value + item.amount * weight
}

data class MinimumCameraEventRequestAdjuster(var minimum: Double) : CameraEventRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelCameraEventRequestAdjuster(var factors: MutableMap<String, Double>) : CameraEventRequestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CameraEventRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCameraEventRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the camera event request steps in order and keeps a trace of each. */
data class CameraEventRequestEngine(
    var steps: MutableList<CameraEventRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventRequest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventRequestEngine(flags: Registry): CameraEventRequestEngine {
    return CameraEventRequestEngine(
        steps = mutableListOf<CameraEventRequestStep>(
            TieredCameraEventRequestAdjuster(
                mutableListOf<Double>(3113.0, 8558.0),
                mutableListOf<Double>(0.87, 1.01),
            ),
            CameraEventRequestAmountOffsetAdjuster(1.98),
            MinimumCameraEventRequestAdjuster(25.4),
            LabelCameraEventRequestAdjuster(
                mutableMapOf<String, Double>("east" to 0.92, "south" to 0.82, "west" to 1.24),
            ),
            FlaggedCameraEventRequestAdjuster(flags, "camera-event-request-seasonal", 6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 17409.6,
    )
}
