package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry

/** One step of the trailer rule calculation. */
interface TrailerRuleModifier {
    fun describe(): String
    fun adjust(item: TrailerRule, value: Double): Double
}

data class TrailerRuleSequenceOffsetAdjuster(var weight: Double) : TrailerRuleModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TrailerRule, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class LabelTrailerRuleAdjuster(var factors: MutableMap<String, Double>) : TrailerRuleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTrailerRuleAdjuster(var percent: Double) : TrailerRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the trailer rule steps in order and keeps a trace of each. */
data class TrailerRuleEngine(
    var steps: MutableList<TrailerRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerRule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerRuleEngine(flags: Registry): TrailerRuleEngine {
    return TrailerRuleEngine(
        steps = mutableListOf<TrailerRuleModifier>(
            TrailerRuleSequenceOffsetAdjuster(0.92),
            LabelTrailerRuleAdjuster(mutableMapOf<String, Double>("online" to 1.36, "west" to 1.38)),
            PercentTrailerRuleAdjuster(-5.0),
            FlaggedTrailerRuleAdjuster(flags, "trailer-rule-tiered", -9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 95709.3,
    )
}
