package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender rule calculation. */
interface TenderRuleFactor {
    fun describe(): String
    fun adjust(item: TenderRule, value: Double): Double
}

data class FlaggedTenderRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTenderRuleAdjuster(var step: Double) : TenderRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderRule, value: Double): Double = roundTo(value, step)
}

data class TenderRuleFuelLitersOffsetAdjuster(var weight: Double) : TenderRuleFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TenderRule, value: Double): Double = value + item.fuelLiters * weight
}

data class CapTenderRuleAdjuster(var cap: Double) : TenderRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderRule, value: Double): Double = minOf(value, cap)
}

/** Runs the tender rule steps in order and keeps a trace of each. */
data class TenderRuleEngine(
    var steps: MutableList<TenderRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TenderRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderRuleEngine(flags: Registry): TenderRuleEngine {
    return TenderRuleEngine(
        steps = mutableListOf<TenderRuleFactor>(
            FlaggedTenderRuleAdjuster(flags, "tender-rule-global", -1.5),
            RoundingTenderRuleAdjuster(0.25),
            TenderRuleFuelLitersOffsetAdjuster(0.84),
            CapTenderRuleAdjuster(19633.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 31976.8,
    )
}
