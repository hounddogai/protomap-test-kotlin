package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the camera event claim calculation. */
interface CameraEventClaimModifier {
    fun describe(): String
    fun adjust(item: CameraEventClaim, value: Double): Double
}

data class DepotCameraEventClaimAdjuster(var factors: MutableMap<String, Double>) : CameraEventClaimModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: CameraEventClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCameraEventClaimAdjuster(var percent: Double) : CameraEventClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCameraEventClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCameraEventClaimAdjuster(var step: Double) : CameraEventClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventClaim, value: Double): Double = roundTo(value, step)
}

data class TieredCameraEventClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCameraEventClaimAdjuster(var minimum: Double) : CameraEventClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the camera event claim steps in order and keeps a trace of each. */
data class CameraEventClaimEngine(
    var steps: MutableList<CameraEventClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventClaimEngine(flags: Registry): CameraEventClaimEngine {
    return CameraEventClaimEngine(
        steps = mutableListOf<CameraEventClaimModifier>(
            DepotCameraEventClaimAdjuster(mutableMapOf<String, Double>("retail" to 1.11)),
            PercentCameraEventClaimAdjuster(-23.2),
            FlaggedCameraEventClaimAdjuster(flags, "camera-event-claim-global", -1.5),
            RoundingCameraEventClaimAdjuster(1.0),
            TieredCameraEventClaimAdjuster(
                mutableListOf<Double>(2210.0, 5850.0, 9411.0),
                mutableListOf<Double>(0.98, 0.94, 1.11),
            ),
            MinimumCameraEventClaimAdjuster(15.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 61920.2,
    )
}
