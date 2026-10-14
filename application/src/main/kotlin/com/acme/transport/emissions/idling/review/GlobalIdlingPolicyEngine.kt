package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global idling policy calculation. */
interface GlobalIdlingPolicyModifier {
    fun describe(): String
    fun adjust(item: GlobalIdlingPolicy, value: Double): Double
}

data class RoundingGlobalIdlingPolicyAdjuster(var step: Double) : GlobalIdlingPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalIdlingPolicy, value: Double): Double = roundTo(value, step)
}

data class TitleGlobalIdlingPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalIdlingPolicyModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalIdlingPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalIdlingPolicyAdjuster(var percent: Double) : GlobalIdlingPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalIdlingPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global idling policy steps in order and keeps a trace of each. */
data class GlobalIdlingPolicyEngine(
    var steps: MutableList<GlobalIdlingPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalIdlingPolicy): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalIdlingPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalIdlingPolicyEngine(): GlobalIdlingPolicyEngine {
    return GlobalIdlingPolicyEngine(
        steps = mutableListOf<GlobalIdlingPolicyModifier>(
            RoundingGlobalIdlingPolicyAdjuster(0.1),
            TitleGlobalIdlingPolicyAdjuster(mutableMapOf<String, Double>("north" to 1.09)),
            PercentGlobalIdlingPolicyAdjuster(-13.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 77447.4,
    )
}
