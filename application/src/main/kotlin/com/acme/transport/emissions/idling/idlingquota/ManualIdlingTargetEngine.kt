package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual idling target calculation. */
interface ManualIdlingTargetStep {
    fun describe(): String
    fun adjust(item: ManualIdlingTarget, value: Double): Double
}

data class RoundingManualIdlingTargetAdjuster(var step: Double) : ManualIdlingTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualIdlingTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualIdlingTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualIdlingTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualIdlingTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualIdlingTargetAdjuster(var percent: Double) : ManualIdlingTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualIdlingTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleManualIdlingTargetAdjuster(var factors: MutableMap<String, Double>) : ManualIdlingTargetStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualIdlingTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual idling target steps in order and keeps a trace of each. */
data class ManualIdlingTargetEngine(
    var steps: MutableList<ManualIdlingTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualIdlingTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualIdlingTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualIdlingTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualIdlingTargetEngine(flags: Registry): ManualIdlingTargetEngine {
    return ManualIdlingTargetEngine(
        steps = mutableListOf<ManualIdlingTargetStep>(
            RoundingManualIdlingTargetAdjuster(5.0),
            FlaggedManualIdlingTargetAdjuster(flags, "manual-idling-target-manual", -4.8),
            PercentManualIdlingTargetAdjuster(-19.2),
            TitleManualIdlingTargetAdjuster(
                mutableMapOf<String, Double>("north" to 1.38, "west" to 1.07, "south" to 1.33),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 71664.0,
    )
}
