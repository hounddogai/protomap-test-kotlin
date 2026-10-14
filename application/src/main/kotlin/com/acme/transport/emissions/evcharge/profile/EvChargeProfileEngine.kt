package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge profile calculation. */
interface EvChargeProfileFactor {
    fun describe(): String
    fun adjust(item: EvChargeProfile, value: Double): Double
}

data class MinimumEvChargeProfileAdjuster(var minimum: Double) : EvChargeProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentEvChargeProfileAdjuster(var percent: Double) : EvChargeProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleEvChargeProfileAdjuster(var factors: MutableMap<String, Double>) : EvChargeProfileFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: EvChargeProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEvChargeProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge profile steps in order and keeps a trace of each. */
data class EvChargeProfileEngine(
    var steps: MutableList<EvChargeProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeProfile): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeProfileEngine(flags: Registry): EvChargeProfileEngine {
    return EvChargeProfileEngine(
        steps = mutableListOf<EvChargeProfileFactor>(
            MinimumEvChargeProfileAdjuster(49.6),
            PercentEvChargeProfileAdjuster(-6.3),
            LocaleEvChargeProfileAdjuster(mutableMapOf<String, Double>("retail" to 0.81, "south" to 1.22)),
            FlaggedEvChargeProfileAdjuster(flags, "ev-charge-profile-global", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 85690.4,
    )
}
