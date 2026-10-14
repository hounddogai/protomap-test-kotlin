package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay rule calculation. */
interface RelayRuleStep {
    fun describe(): String
    fun adjust(item: RelayRule, value: Double): Double
}

data class CapRelayRuleAdjuster(var cap: Double) : RelayRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayRule, value: Double): Double = minOf(value, cap)
}

data class RelayRuleCeilingOffsetAdjuster(var weight: Double) : RelayRuleStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RelayRule, value: Double): Double = value + item.ceiling * weight
}

data class PercentRelayRuleAdjuster(var percent: Double) : RelayRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRelayRuleAdjuster(var step: Double) : RelayRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayRule, value: Double): Double = roundTo(value, step)
}

/** Runs the relay rule steps in order and keeps a trace of each. */
data class RelayRuleEngine(
    var steps: MutableList<RelayRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayRule): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayRuleEngine(): RelayRuleEngine {
    return RelayRuleEngine(
        steps = mutableListOf<RelayRuleStep>(
            CapRelayRuleAdjuster(39897.0),
            RelayRuleCeilingOffsetAdjuster(1.47),
            PercentRelayRuleAdjuster(-13.4),
            RoundingRelayRuleAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 10472.4,
    )
}
