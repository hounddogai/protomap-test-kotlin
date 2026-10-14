package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending node bundle calculation. */
interface PendingNodeBundleAdjuster {
    fun describe(): String
    fun adjust(item: PendingNodeBundle, value: Double): Double
}

data class MinimumPendingNodeBundleAdjuster(var minimum: Double) : PendingNodeBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingNodeBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingNodeBundleAdjuster(var cap: Double) : PendingNodeBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingNodeBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingNodeBundleAdjuster(var step: Double) : PendingNodeBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingNodeBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingNodeBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingNodeBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingNodeBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending node bundle steps in order and keeps a trace of each. */
data class PendingNodeBundleEngine(
    var steps: MutableList<PendingNodeBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingNodeBundle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingNodeBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingNodeBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingNodeBundleEngine(flags: Registry): PendingNodeBundleEngine {
    return PendingNodeBundleEngine(
        steps = mutableListOf<PendingNodeBundleAdjuster>(
            MinimumPendingNodeBundleAdjuster(15.0),
            CapPendingNodeBundleAdjuster(37239.0),
            RoundingPendingNodeBundleAdjuster(1.0),
            FlaggedPendingNodeBundleAdjuster(flags, "pending-node-bundle-manual", 0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 81920.0,
    )
}
