package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

/** One step of the global backhaul preference calculation. */
interface GlobalBackhaulPreferenceStep {
    fun describe(): String
    fun adjust(item: GlobalBackhaulPreference, value: Double): Double
}

data class TieredGlobalBackhaulPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalBackhaulPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalBackhaulPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelGlobalBackhaulPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalBackhaulPreferenceStep {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalBackhaulPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalBackhaulPreferenceAdjuster(var cap: Double) : GlobalBackhaulPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBackhaulPreference, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalBackhaulPreferenceAdjuster(var percent: Double) : GlobalBackhaulPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalBackhaulPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalBackhaulPreferenceRevisionOffsetAdjuster(var weight: Double) : GlobalBackhaulPreferenceStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalBackhaulPreference, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the global backhaul preference steps in order and keeps a trace of each. */
data class GlobalBackhaulPreferenceEngine(
    var steps: MutableList<GlobalBackhaulPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalBackhaulPreference): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalBackhaulPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalBackhaulPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalBackhaulPreferenceEngine(): GlobalBackhaulPreferenceEngine {
    return GlobalBackhaulPreferenceEngine(
        steps = mutableListOf<GlobalBackhaulPreferenceStep>(
            TieredGlobalBackhaulPreferenceAdjuster(
                mutableListOf<Double>(3378.0, 3648.0),
                mutableListOf<Double>(1.08, 1.07),
            ),
            LabelGlobalBackhaulPreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 1.31, "west" to 0.79, "online" to 1.39),
            ),
            CapGlobalBackhaulPreferenceAdjuster(33869.0),
            PercentGlobalBackhaulPreferenceAdjuster(3.0),
            GlobalBackhaulPreferenceRevisionOffsetAdjuster(0.93),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 63544.4,
    )
}
