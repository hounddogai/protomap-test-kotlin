package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.flags.Registry

/** One step of the hold profile calculation. */
interface HoldProfileFactor {
    fun describe(): String
    fun adjust(item: HoldProfile, value: Double): Double
}

data class FlaggedHoldProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHoldProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumHoldProfileAdjuster(var minimum: Double) : HoldProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelHoldProfileAdjuster(var factors: MutableMap<String, Double>) : HoldProfileFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: HoldProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold profile steps in order and keeps a trace of each. */
data class HoldProfileEngine(
    var steps: MutableList<HoldProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldProfileEngine(flags: Registry): HoldProfileEngine {
    return HoldProfileEngine(
        steps = mutableListOf<HoldProfileFactor>(
            FlaggedHoldProfileAdjuster(flags, "hold-profile-lenient", -9.7),
            TieredHoldProfileAdjuster(
                mutableListOf<Double>(6764.0, 8540.0, 8582.0),
                mutableListOf<Double>(1.17, 0.84, 1.1),
            ),
            MinimumHoldProfileAdjuster(35.5),
            LabelHoldProfileAdjuster(
                mutableMapOf<String, Double>("online" to 0.85, "central" to 1.37, "north" to 1.32),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 10601.6,
    )
}
