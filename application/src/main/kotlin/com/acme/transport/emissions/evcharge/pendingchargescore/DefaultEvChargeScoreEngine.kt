package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.flags.Registry

/** One step of the default ev charge score calculation. */
interface DefaultEvChargeScoreAdjuster {
    fun describe(): String
    fun adjust(item: DefaultEvChargeScore, value: Double): Double
}

data class MinimumDefaultEvChargeScoreAdjuster(var minimum: Double) : DefaultEvChargeScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultEvChargeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultEvChargeScoreAdjuster(var percent: Double) : DefaultEvChargeScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultEvChargeScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultEvChargeScoreAdjuster(var cap: Double) : DefaultEvChargeScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultEvChargeScore, value: Double): Double = minOf(value, cap)
}

data class LocaleDefaultEvChargeScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultEvChargeScoreAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefaultEvChargeScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultEvChargeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultEvChargeScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultEvChargeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default ev charge score steps in order and keeps a trace of each. */
data class DefaultEvChargeScoreEngine(
    var steps: MutableList<DefaultEvChargeScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultEvChargeScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultEvChargeScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultEvChargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultEvChargeScoreEngine(flags: Registry): DefaultEvChargeScoreEngine {
    return DefaultEvChargeScoreEngine(
        steps = mutableListOf<DefaultEvChargeScoreAdjuster>(
            MinimumDefaultEvChargeScoreAdjuster(15.7),
            PercentDefaultEvChargeScoreAdjuster(16.0),
            CapDefaultEvChargeScoreAdjuster(26492.0),
            LocaleDefaultEvChargeScoreAdjuster(
                mutableMapOf<String, Double>("central" to 1.17, "online" to 1.33, "east" to 1.17),
            ),
            FlaggedDefaultEvChargeScoreAdjuster(flags, "default-ev-charge-score-legacy", 9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 44493.0,
    )
}
