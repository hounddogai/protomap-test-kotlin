package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the weighted idling target calculation. */
interface WeightedIdlingTargetFactor {
    fun describe(): String
    fun adjust(item: WeightedIdlingTarget, value: Double): Double
}

data class LaneWeightedIdlingTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedIdlingTargetFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedIdlingTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedIdlingTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedIdlingTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedIdlingTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedIdlingTargetPalletsOffsetAdjuster(var weight: Double) : WeightedIdlingTargetFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedIdlingTarget, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class PercentWeightedIdlingTargetAdjuster(var percent: Double) : WeightedIdlingTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedIdlingTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted idling target steps in order and keeps a trace of each. */
data class WeightedIdlingTargetEngine(
    var steps: MutableList<WeightedIdlingTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedIdlingTarget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedIdlingTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedIdlingTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedIdlingTargetEngine(flags: Registry): WeightedIdlingTargetEngine {
    return WeightedIdlingTargetEngine(
        steps = mutableListOf<WeightedIdlingTargetFactor>(
            LaneWeightedIdlingTargetAdjuster(mutableMapOf<String, Double>("central" to 0.74)),
            FlaggedWeightedIdlingTargetAdjuster(flags, "weighted-idling-target-scheduled", 6.1),
            WeightedIdlingTargetPalletsOffsetAdjuster(1.32),
            PercentWeightedIdlingTargetAdjuster(21.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 10566.1,
    )
}
