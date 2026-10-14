package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default capacity preference calculation. */
interface DefaultCapacityPreferenceModifier {
    fun describe(): String
    fun adjust(item: DefaultCapacityPreference, value: Double): Double
}

data class RoundingDefaultCapacityPreferenceAdjuster(var step: Double) : DefaultCapacityPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCapacityPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultCapacityPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCapacityPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCapacityPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultCapacityPreferenceAdjuster(var cap: Double) : DefaultCapacityPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCapacityPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the default capacity preference steps in order and keeps a trace of each. */
data class DefaultCapacityPreferenceEngine(
    var steps: MutableList<DefaultCapacityPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCapacityPreference): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCapacityPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCapacityPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCapacityPreferenceEngine(flags: Registry): DefaultCapacityPreferenceEngine {
    return DefaultCapacityPreferenceEngine(
        steps = mutableListOf<DefaultCapacityPreferenceModifier>(
            RoundingDefaultCapacityPreferenceAdjuster(5.0),
            FlaggedDefaultCapacityPreferenceAdjuster(flags, "default-capacity-preference-adaptive", 4.4),
            CapDefaultCapacityPreferenceAdjuster(37827.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 4794.6,
    )
}
