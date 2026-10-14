package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.flags.Registry

/** One step of the strict hold bundle calculation. */
interface StrictHoldBundleAdjuster {
    fun describe(): String
    fun adjust(item: StrictHoldBundle, value: Double): Double
}

data class CapStrictHoldBundleAdjuster(var cap: Double) : StrictHoldBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictHoldBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictHoldBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictHoldBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictHoldBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictHoldBundleAdjuster(var percent: Double) : StrictHoldBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictHoldBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictHoldBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictHoldBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictHoldBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictHoldBundleAdjuster(var minimum: Double) : StrictHoldBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictHoldBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict hold bundle steps in order and keeps a trace of each. */
data class StrictHoldBundleEngine(
    var steps: MutableList<StrictHoldBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictHoldBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictHoldBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictHoldBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictHoldBundleEngine(flags: Registry): StrictHoldBundleEngine {
    return StrictHoldBundleEngine(
        steps = mutableListOf<StrictHoldBundleAdjuster>(
            CapStrictHoldBundleAdjuster(31224.0),
            FlaggedStrictHoldBundleAdjuster(flags, "strict-hold-bundle-strict", 3.3),
            PercentStrictHoldBundleAdjuster(0.5),
            TieredStrictHoldBundleAdjuster(
                mutableListOf<Double>(3748.0, 4899.0, 6306.0, 6496.0),
                mutableListOf<Double>(0.94, 1.2, 0.98, 1.18),
            ),
            MinimumStrictHoldBundleAdjuster(2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 46072.3,
    )
}
