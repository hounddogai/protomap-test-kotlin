package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered cross dock rule calculation. */
interface TieredCrossDockRuleStep {
    fun describe(): String
    fun adjust(item: TieredCrossDockRule, value: Double): Double
}

data class TieredTieredCrossDockRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCrossDockRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCrossDockRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelTieredCrossDockRuleAdjuster(var factors: MutableMap<String, Double>) : TieredCrossDockRuleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredCrossDockRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredCrossDockRuleAdjuster(var step: Double) : TieredCrossDockRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCrossDockRule, value: Double): Double = roundTo(value, step)
}

data class CapTieredCrossDockRuleAdjuster(var cap: Double) : TieredCrossDockRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCrossDockRule, value: Double): Double = minOf(value, cap)
}

data class PercentTieredCrossDockRuleAdjuster(var percent: Double) : TieredCrossDockRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCrossDockRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered cross dock rule steps in order and keeps a trace of each. */
data class TieredCrossDockRuleEngine(
    var steps: MutableList<TieredCrossDockRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredCrossDockRule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCrossDockRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredCrossDockRuleEngine(): TieredCrossDockRuleEngine {
    return TieredCrossDockRuleEngine(
        steps = mutableListOf<TieredCrossDockRuleStep>(
            TieredTieredCrossDockRuleAdjuster(
                mutableListOf<Double>(3780.0, 3836.0, 5231.0, 6109.0),
                mutableListOf<Double>(1.17, 1.01, 1.14, 0.93),
            ),
            LabelTieredCrossDockRuleAdjuster(
                mutableMapOf<String, Double>("west" to 1.22, "online" to 1.15, "central" to 1.35),
            ),
            RoundingTieredCrossDockRuleAdjuster(0.05),
            CapTieredCrossDockRuleAdjuster(8957.0),
            PercentTieredCrossDockRuleAdjuster(-12.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 60368.0,
    )
}
