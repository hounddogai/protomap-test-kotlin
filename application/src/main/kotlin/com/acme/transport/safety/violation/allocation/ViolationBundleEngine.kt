package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the violation bundle calculation. */
interface ViolationBundleFactor {
    fun describe(): String
    fun adjust(item: ViolationBundle, value: Double): Double
}

data class ViolationBundleBudgetOffsetAdjuster(var weight: Double) : ViolationBundleFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ViolationBundle, value: Double): Double = value + item.budget * weight
}

data class FlaggedViolationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentViolationBundleAdjuster(var percent: Double) : ViolationBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the violation bundle steps in order and keeps a trace of each. */
data class ViolationBundleEngine(
    var steps: MutableList<ViolationBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationBundle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationBundleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationBundleEngine(flags: Registry): ViolationBundleEngine {
    return ViolationBundleEngine(
        steps = mutableListOf<ViolationBundleFactor>(
            ViolationBundleBudgetOffsetAdjuster(0.59),
            FlaggedViolationBundleAdjuster(flags, "violation-bundle-seasonal", -1.3),
            PercentViolationBundleAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 11979.9,
    )
}
