package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual camera event policy calculation. */
interface ManualCameraEventPolicyAdjuster {
    fun describe(): String
    fun adjust(item: ManualCameraEventPolicy, value: Double): Double
}

data class SourceManualCameraEventPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualCameraEventPolicyAdjuster(var minimum: Double) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualCameraEventPolicyAdjuster(var step: Double) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualCameraEventPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualCameraEventPolicyCeilingOffsetAdjuster(var weight: Double) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double = value + item.ceiling * weight
}

data class TieredManualCameraEventPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCameraEventPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCameraEventPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual camera event policy steps in order and keeps a trace of each. */
data class ManualCameraEventPolicyEngine(
    var steps: MutableList<ManualCameraEventPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualCameraEventPolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCameraEventPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCameraEventPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualCameraEventPolicyEngine(flags: Registry): ManualCameraEventPolicyEngine {
    return ManualCameraEventPolicyEngine(
        steps = mutableListOf<ManualCameraEventPolicyAdjuster>(
            SourceManualCameraEventPolicyAdjuster(mutableMapOf<String, Double>("online" to 0.71)),
            MinimumManualCameraEventPolicyAdjuster(27.5),
            RoundingManualCameraEventPolicyAdjuster(0.1),
            FlaggedManualCameraEventPolicyAdjuster(flags, "manual-camera-event-policy-tiered", -0.5),
            ManualCameraEventPolicyCeilingOffsetAdjuster(0.66),
            TieredManualCameraEventPolicyAdjuster(
                mutableListOf<Double>(1095.0, 5783.0, 6160.0, 9369.0),
                mutableListOf<Double>(0.8, 1.08, 0.81, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 13067.4,
    )
}
