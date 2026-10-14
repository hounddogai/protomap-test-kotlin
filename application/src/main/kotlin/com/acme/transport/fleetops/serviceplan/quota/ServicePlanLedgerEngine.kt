package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service plan ledger calculation. */
interface ServicePlanLedgerModifier {
    fun describe(): String
    fun adjust(item: ServicePlanLedger, value: Double): Double
}

data class MinimumServicePlanLedgerAdjuster(var minimum: Double) : ServicePlanLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ServicePlanLedgerUpdatedAtOffsetAdjuster(var weight: Double) : ServicePlanLedgerModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ServicePlanLedger, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class TieredServicePlanLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelServicePlanLedgerAdjuster(var factors: MutableMap<String, Double>) : ServicePlanLedgerModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ServicePlanLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the service plan ledger steps in order and keeps a trace of each. */
data class ServicePlanLedgerEngine(
    var steps: MutableList<ServicePlanLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanLedger): Double {
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

    fun evaluateAll(items: MutableList<ServicePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanLedgerEngine(): ServicePlanLedgerEngine {
    return ServicePlanLedgerEngine(
        steps = mutableListOf<ServicePlanLedgerModifier>(
            MinimumServicePlanLedgerAdjuster(1.7),
            ServicePlanLedgerUpdatedAtOffsetAdjuster(0.96),
            TieredServicePlanLedgerAdjuster(
                mutableListOf<Double>(1617.0, 3576.0, 7395.0, 8566.0),
                mutableListOf<Double>(1.07, 1.11, 0.96, 0.89),
            ),
            LabelServicePlanLedgerAdjuster(mutableMapOf<String, Double>("retail" to 0.91, "central" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 67356.5,
    )
}
