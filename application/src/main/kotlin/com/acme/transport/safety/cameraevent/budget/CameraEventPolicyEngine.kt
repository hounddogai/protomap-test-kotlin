package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.flags.Registry

/** One step of the camera event policy calculation. */
interface CameraEventPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventPolicy, value: Double): Double
}

data class PercentCameraEventPolicyAdjuster(var percent: Double) : CameraEventPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameCameraEventPolicyAdjuster(var factors: MutableMap<String, Double>) : CameraEventPolicyAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CameraEventPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCameraEventPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CameraEventPolicyEndsAtOffsetAdjuster(var weight: Double) : CameraEventPolicyAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CameraEventPolicy, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the camera event policy steps in order and keeps a trace of each. */
data class CameraEventPolicyEngine(
    var steps: MutableList<CameraEventPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventPolicy): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventPolicyEngine(flags: Registry): CameraEventPolicyEngine {
    return CameraEventPolicyEngine(
        steps = mutableListOf<CameraEventPolicyAdjuster>(
            PercentCameraEventPolicyAdjuster(20.6),
            NameCameraEventPolicyAdjuster(mutableMapOf<String, Double>("west" to 1.34)),
            FlaggedCameraEventPolicyAdjuster(flags, "camera-event-policy-default", -9.2),
            CameraEventPolicyEndsAtOffsetAdjuster(1.72),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 74682.9,
    )
}
