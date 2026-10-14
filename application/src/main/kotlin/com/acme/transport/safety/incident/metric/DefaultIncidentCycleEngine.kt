package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default incident cycle calculation. */
interface DefaultIncidentCycleStep {
    fun describe(): String
    fun adjust(item: DefaultIncidentCycle, value: Double): Double
}

data class NameDefaultIncidentCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultIncidentCycleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultIncidentCycleAdjuster(var step: Double) : DefaultIncidentCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double = roundTo(value, step)
}

data class DefaultIncidentCycleScoreOffsetAdjuster(var weight: Double) : DefaultIncidentCycleStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double = value + item.score * weight
}

data class PercentDefaultIncidentCycleAdjuster(var percent: Double) : DefaultIncidentCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultIncidentCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultIncidentCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultIncidentCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultIncidentCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultIncidentCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default incident cycle steps in order and keeps a trace of each. */
data class DefaultIncidentCycleEngine(
    var steps: MutableList<DefaultIncidentCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultIncidentCycle): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultIncidentCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultIncidentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultIncidentCycleEngine(flags: Registry): DefaultIncidentCycleEngine {
    return DefaultIncidentCycleEngine(
        steps = mutableListOf<DefaultIncidentCycleStep>(
            NameDefaultIncidentCycleAdjuster(mutableMapOf<String, Double>("west" to 0.93)),
            RoundingDefaultIncidentCycleAdjuster(1.0),
            DefaultIncidentCycleScoreOffsetAdjuster(0.53),
            PercentDefaultIncidentCycleAdjuster(23.0),
            TieredDefaultIncidentCycleAdjuster(
                mutableListOf<Double>(5445.0, 6194.0, 8112.0),
                mutableListOf<Double>(1.11, 1.06, 0.99),
            ),
            FlaggedDefaultIncidentCycleAdjuster(flags, "default-incident-cycle-tiered", 0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 49718.5,
    )
}
