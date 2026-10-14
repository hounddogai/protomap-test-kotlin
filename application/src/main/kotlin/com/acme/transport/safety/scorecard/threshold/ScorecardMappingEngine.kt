package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard mapping calculation. */
interface ScorecardMappingModifier {
    fun describe(): String
    fun adjust(item: ScorecardMapping, value: Double): Double
}

data class RoundingScorecardMappingAdjuster(var step: Double) : ScorecardMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardMapping, value: Double): Double = roundTo(value, step)
}

data class PercentScorecardMappingAdjuster(var percent: Double) : ScorecardMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelScorecardMappingAdjuster(var factors: MutableMap<String, Double>) : ScorecardMappingModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScorecardMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScorecardMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scorecard mapping steps in order and keeps a trace of each. */
data class ScorecardMappingEngine(
    var steps: MutableList<ScorecardMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScorecardMapping): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScorecardMappingEngine(flags: Registry): ScorecardMappingEngine {
    return ScorecardMappingEngine(
        steps = mutableListOf<ScorecardMappingModifier>(
            RoundingScorecardMappingAdjuster(0.5),
            PercentScorecardMappingAdjuster(-6.9),
            LabelScorecardMappingAdjuster(
                mutableMapOf<String, Double>("north" to 0.94, "online" to 0.72, "west" to 0.82),
            ),
            FlaggedScorecardMappingAdjuster(flags, "scorecard-mapping-global", -4.5),
            TieredScorecardMappingAdjuster(
                mutableListOf<Double>(171.0, 6970.0, 9511.0),
                mutableListOf<Double>(1.12, 1.1, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 29815.4,
    )
}
