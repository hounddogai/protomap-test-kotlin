package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge plan calculation. */
interface EvChargePlanFactor {
    fun describe(): String
    fun adjust(item: EvChargePlan, value: Double): Double
}

data class EvChargePlanAttemptsOffsetAdjuster(var weight: Double) : EvChargePlanFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: EvChargePlan, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class PercentEvChargePlanAdjuster(var percent: Double) : EvChargePlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumEvChargePlanAdjuster(var minimum: Double) : EvChargePlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEvChargePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargePlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge plan steps in order and keeps a trace of each. */
data class EvChargePlanEngine(
    var steps: MutableList<EvChargePlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargePlan): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargePlanEngine(flags: Registry): EvChargePlanEngine {
    return EvChargePlanEngine(
        steps = mutableListOf<EvChargePlanFactor>(
            EvChargePlanAttemptsOffsetAdjuster(1.36),
            PercentEvChargePlanAdjuster(8.6),
            MinimumEvChargePlanAdjuster(17.8),
            FlaggedEvChargePlanAdjuster(flags, "ev-charge-plan-legacy", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 70604.4,
    )
}
