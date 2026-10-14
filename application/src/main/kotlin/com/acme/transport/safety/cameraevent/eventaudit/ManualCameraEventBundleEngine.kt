package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.Registry

/** One step of the manual camera event bundle calculation. */
interface ManualCameraEventBundleAdjuster {
    fun describe(): String
    fun adjust(item: ManualCameraEventBundle, value: Double): Double
}

data class MinimumManualCameraEventBundleAdjuster(var minimum: Double) : ManualCameraEventBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCameraEventBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualCameraEventBundleAmountOffsetAdjuster(var weight: Double) : ManualCameraEventBundleAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ManualCameraEventBundle, value: Double): Double = value + item.amount * weight
}

data class FlaggedManualCameraEventBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCameraEventBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCameraEventBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualCameraEventBundleAdjuster(var cap: Double) : ManualCameraEventBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCameraEventBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the manual camera event bundle steps in order and keeps a trace of each. */
data class ManualCameraEventBundleEngine(
    var steps: MutableList<ManualCameraEventBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualCameraEventBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCameraEventBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCameraEventBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualCameraEventBundleEngine(flags: Registry): ManualCameraEventBundleEngine {
    return ManualCameraEventBundleEngine(
        steps = mutableListOf<ManualCameraEventBundleAdjuster>(
            MinimumManualCameraEventBundleAdjuster(3.2),
            ManualCameraEventBundleAmountOffsetAdjuster(0.08),
            FlaggedManualCameraEventBundleAdjuster(flags, "manual-camera-event-bundle-seasonal", -5.5),
            CapManualCameraEventBundleAdjuster(36598.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 8301.3,
    )
}
