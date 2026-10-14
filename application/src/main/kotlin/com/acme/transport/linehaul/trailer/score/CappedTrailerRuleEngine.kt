package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped trailer rule calculation. */
interface CappedTrailerRuleModifier {
    fun describe(): String
    fun adjust(item: CappedTrailerRule, value: Double): Double
}

data class SourceCappedTrailerRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerRuleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedTrailerRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedTrailerRuleAdjuster(var percent: Double) : CappedTrailerRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTrailerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedTrailerRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedTrailerRuleSequenceOffsetAdjuster(var weight: Double) : CappedTrailerRuleModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: CappedTrailerRule, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredCappedTrailerRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTrailerRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTrailerRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped trailer rule steps in order and keeps a trace of each. */
data class CappedTrailerRuleEngine(
    var steps: MutableList<CappedTrailerRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTrailerRule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTrailerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTrailerRuleEngine(flags: Registry): CappedTrailerRuleEngine {
    return CappedTrailerRuleEngine(
        steps = mutableListOf<CappedTrailerRuleModifier>(
            SourceCappedTrailerRuleAdjuster(mutableMapOf<String, Double>("retail" to 1.4)),
            PercentCappedTrailerRuleAdjuster(16.7),
            FlaggedCappedTrailerRuleAdjuster(flags, "capped-trailer-rule-adaptive", -8.9),
            CappedTrailerRuleSequenceOffsetAdjuster(1.49),
            TieredCappedTrailerRuleAdjuster(
                mutableListOf<Double>(3528.0, 8635.0, 9083.0),
                mutableListOf<Double>(1.19, 0.85, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 94050.5,
    )
}
