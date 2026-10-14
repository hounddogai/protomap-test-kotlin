package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.Registry

/** One step of the pending near miss threshold calculation. */
interface PendingNearMissThresholdFactor {
    fun describe(): String
    fun adjust(item: PendingNearMissThreshold, value: Double): Double
}

data class CapPendingNearMissThresholdAdjuster(var cap: Double) : PendingNearMissThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingNearMissThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingNearMissThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingNearMissThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingNearMissThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NamePendingNearMissThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingNearMissThresholdFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingNearMissThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending near miss threshold steps in order and keeps a trace of each. */
data class PendingNearMissThresholdEngine(
    var steps: MutableList<PendingNearMissThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingNearMissThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingNearMissThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingNearMissThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingNearMissThresholdEngine(flags: Registry): PendingNearMissThresholdEngine {
    return PendingNearMissThresholdEngine(
        steps = mutableListOf<PendingNearMissThresholdFactor>(
            CapPendingNearMissThresholdAdjuster(46984.0),
            FlaggedPendingNearMissThresholdAdjuster(flags, "pending-near-miss-threshold-tiered", -4.7),
            NamePendingNearMissThresholdAdjuster(
                mutableMapOf<String, Double>("central" to 1.25, "north" to 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 74270.5,
    )
}
