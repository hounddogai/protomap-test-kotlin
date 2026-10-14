package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal broker plan calculation. */
interface SeasonalBrokerPlanFactor {
    fun describe(): String
    fun adjust(item: SeasonalBrokerPlan, value: Double): Double
}

data class NameSeasonalBrokerPlanAdjuster(var factors: MutableMap<String, Double>) : SeasonalBrokerPlanFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalBrokerPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalBrokerPlanAdjuster(var percent: Double) : SeasonalBrokerPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalBrokerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalBrokerPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalBrokerPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalBrokerPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalBrokerPlanAdjuster(var cap: Double) : SeasonalBrokerPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalBrokerPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal broker plan steps in order and keeps a trace of each. */
data class SeasonalBrokerPlanEngine(
    var steps: MutableList<SeasonalBrokerPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalBrokerPlan): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalBrokerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalBrokerPlanEngine(flags: Registry): SeasonalBrokerPlanEngine {
    return SeasonalBrokerPlanEngine(
        steps = mutableListOf<SeasonalBrokerPlanFactor>(
            NameSeasonalBrokerPlanAdjuster(mutableMapOf<String, Double>("south" to 1.07)),
            PercentSeasonalBrokerPlanAdjuster(5.1),
            FlaggedSeasonalBrokerPlanAdjuster(flags, "seasonal-broker-plan-scheduled", 3.7),
            CapSeasonalBrokerPlanAdjuster(36804.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 21899.9,
    )
}
