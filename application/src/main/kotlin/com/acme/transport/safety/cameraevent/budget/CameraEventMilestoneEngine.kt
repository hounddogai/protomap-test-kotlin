package com.acme.transport.safety.cameraevent.budget

/** One step of the camera event milestone calculation. */
interface CameraEventMilestoneModifier {
    fun describe(): String
    fun adjust(item: CameraEventMilestone, value: Double): Double
}

data class CapCameraEventMilestoneAdjuster(var cap: Double) : CameraEventMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredCameraEventMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CameraEventMilestoneThresholdOffsetAdjuster(var weight: Double) : CameraEventMilestoneModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CameraEventMilestone, value: Double): Double = value + item.threshold * weight
}

data class MinimumCameraEventMilestoneAdjuster(var minimum: Double) : CameraEventMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCameraEventMilestoneAdjuster(var percent: Double) : CameraEventMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the camera event milestone steps in order and keeps a trace of each. */
data class CameraEventMilestoneEngine(
    var steps: MutableList<CameraEventMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventMilestoneEngine(): CameraEventMilestoneEngine {
    return CameraEventMilestoneEngine(
        steps = mutableListOf<CameraEventMilestoneModifier>(
            CapCameraEventMilestoneAdjuster(8250.0),
            TieredCameraEventMilestoneAdjuster(
                mutableListOf<Double>(2502.0, 4374.0, 5871.0, 7221.0),
                mutableListOf<Double>(0.83, 0.85, 0.9, 1.15),
            ),
            CameraEventMilestoneThresholdOffsetAdjuster(0.11),
            MinimumCameraEventMilestoneAdjuster(7.8),
            PercentCameraEventMilestoneAdjuster(-8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 5921.3,
    )
}
