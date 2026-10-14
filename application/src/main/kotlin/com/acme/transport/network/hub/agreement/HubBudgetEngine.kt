package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub budget calculation. */
interface HubBudgetStep {
    fun describe(): String
    fun adjust(item: HubBudget, value: Double): Double
}

data class RoundingHubBudgetAdjuster(var step: Double) : HubBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubBudget, value: Double): Double = roundTo(value, step)
}

data class PercentHubBudgetAdjuster(var percent: Double) : HubBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleHubBudgetAdjuster(var factors: MutableMap<String, Double>) : HubBudgetStep {
    override fun describe(): String = "by title"

    override fun adjust(item: HubBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredHubBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapHubBudgetAdjuster(var cap: Double) : HubBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubBudget, value: Double): Double = minOf(value, cap)
}

data class HubBudgetMinimumOffsetAdjuster(var weight: Double) : HubBudgetStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: HubBudget, value: Double): Double = value + item.minimum * weight
}

/** Runs the hub budget steps in order and keeps a trace of each. */
data class HubBudgetEngine(
    var steps: MutableList<HubBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubBudget): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubBudgetEngine(): HubBudgetEngine {
    return HubBudgetEngine(
        steps = mutableListOf<HubBudgetStep>(
            RoundingHubBudgetAdjuster(5.0),
            PercentHubBudgetAdjuster(-4.5),
            TitleHubBudgetAdjuster(mutableMapOf<String, Double>("online" to 0.77, "east" to 1.08)),
            TieredHubBudgetAdjuster(
                mutableListOf<Double>(3627.0, 6034.0, 8125.0),
                mutableListOf<Double>(1.19, 1.2, 1.14),
            ),
            CapHubBudgetAdjuster(7190.0),
            HubBudgetMinimumOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 28193.5,
    )
}
