package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area rule calculation. */
interface ServiceAreaRuleAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaRule, value: Double): Double
}

data class FlaggedServiceAreaRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitServiceAreaRuleAdjuster(var factors: MutableMap<String, Double>) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: ServiceAreaRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredServiceAreaRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ServiceAreaRuleRetriesOffsetAdjuster(var weight: Double) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ServiceAreaRule, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentServiceAreaRuleAdjuster(var percent: Double) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingServiceAreaRuleAdjuster(var step: Double) : ServiceAreaRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaRule, value: Double): Double = roundTo(value, step)
}

/** Runs the service area rule steps in order and keeps a trace of each. */
data class ServiceAreaRuleEngine(
    var steps: MutableList<ServiceAreaRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaRuleEngine(flags: Registry): ServiceAreaRuleEngine {
    return ServiceAreaRuleEngine(
        steps = mutableListOf<ServiceAreaRuleAdjuster>(
            FlaggedServiceAreaRuleAdjuster(flags, "service-area-rule-strict", -1.6),
            UnitServiceAreaRuleAdjuster(mutableMapOf<String, Double>("retail" to 1.31, "north" to 1.25)),
            TieredServiceAreaRuleAdjuster(
                mutableListOf<Double>(646.0, 716.0, 5299.0),
                mutableListOf<Double>(0.92, 1.19, 1.17),
            ),
            ServiceAreaRuleRetriesOffsetAdjuster(1.05),
            PercentServiceAreaRuleAdjuster(-22.2),
            RoundingServiceAreaRuleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 23208.9,
    )
}
