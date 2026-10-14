package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional broker audit calculation. */
interface RegionalBrokerAuditStep {
    fun describe(): String
    fun adjust(item: RegionalBrokerAudit, value: Double): Double
}

data class RegionalBrokerAuditScoreOffsetAdjuster(var weight: Double) : RegionalBrokerAuditStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalBrokerAudit, value: Double): Double = value + item.score * weight
}

data class TitleRegionalBrokerAuditAdjuster(var factors: MutableMap<String, Double>) : RegionalBrokerAuditStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalBrokerAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalBrokerAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalBrokerAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalBrokerAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalBrokerAuditAdjuster(var cap: Double) : RegionalBrokerAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalBrokerAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the regional broker audit steps in order and keeps a trace of each. */
data class RegionalBrokerAuditEngine(
    var steps: MutableList<RegionalBrokerAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalBrokerAudit): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalBrokerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalBrokerAuditEngine(): RegionalBrokerAuditEngine {
    return RegionalBrokerAuditEngine(
        steps = mutableListOf<RegionalBrokerAuditStep>(
            RegionalBrokerAuditScoreOffsetAdjuster(1.01),
            TitleRegionalBrokerAuditAdjuster(mutableMapOf<String, Double>("west" to 1.3, "north" to 0.84)),
            TieredRegionalBrokerAuditAdjuster(
                mutableListOf<Double>(998.0, 1061.0, 3473.0, 6275.0),
                mutableListOf<Double>(0.92, 1.18, 1.14, 1.11),
            ),
            CapRegionalBrokerAuditAdjuster(45047.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 14051.2,
    )
}
