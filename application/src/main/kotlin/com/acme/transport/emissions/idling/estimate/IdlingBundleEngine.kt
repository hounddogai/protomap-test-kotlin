package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the idling bundle calculation. */
interface IdlingBundleAdjuster {
    fun describe(): String
    fun adjust(item: IdlingBundle, value: Double): Double
}

data class CapIdlingBundleAdjuster(var cap: Double) : IdlingBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingBundle, value: Double): Double = minOf(value, cap)
}

data class TieredIdlingBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class IdlingBundleCreatedAtOffsetAdjuster(var weight: Double) : IdlingBundleAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: IdlingBundle, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class FlaggedIdlingBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneIdlingBundleAdjuster(var factors: MutableMap<String, Double>) : IdlingBundleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: IdlingBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the idling bundle steps in order and keeps a trace of each. */
data class IdlingBundleEngine(
    var steps: MutableList<IdlingBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingBundle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingBundleEngine(flags: Registry): IdlingBundleEngine {
    return IdlingBundleEngine(
        steps = mutableListOf<IdlingBundleAdjuster>(
            CapIdlingBundleAdjuster(9046.0),
            TieredIdlingBundleAdjuster(mutableListOf<Double>(7251.0, 8611.0), mutableListOf<Double>(1.1, 1.0)),
            IdlingBundleCreatedAtOffsetAdjuster(0.72),
            FlaggedIdlingBundleAdjuster(flags, "idling-bundle-capped", 1.1),
            LaneIdlingBundleAdjuster(mutableMapOf<String, Double>("north" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 33496.4,
    )
}
