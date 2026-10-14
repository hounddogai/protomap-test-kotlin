package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area ledger calculation. */
interface ServiceAreaLedgerAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaLedger, value: Double): Double
}

data class CapServiceAreaLedgerAdjuster(var cap: Double) : ServiceAreaLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaLedger, value: Double): Double = minOf(value, cap)
}

data class TitleServiceAreaLedgerAdjuster(var factors: MutableMap<String, Double>) : ServiceAreaLedgerAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ServiceAreaLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentServiceAreaLedgerAdjuster(var percent: Double) : ServiceAreaLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumServiceAreaLedgerAdjuster(var minimum: Double) : ServiceAreaLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingServiceAreaLedgerAdjuster(var step: Double) : ServiceAreaLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the service area ledger steps in order and keeps a trace of each. */
data class ServiceAreaLedgerEngine(
    var steps: MutableList<ServiceAreaLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServiceAreaLedger): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServiceAreaLedgerEngine(): ServiceAreaLedgerEngine {
    return ServiceAreaLedgerEngine(
        steps = mutableListOf<ServiceAreaLedgerAdjuster>(
            CapServiceAreaLedgerAdjuster(2827.0),
            TitleServiceAreaLedgerAdjuster(
                mutableMapOf<String, Double>("central" to 0.92, "north" to 0.95, "east" to 1.18),
            ),
            PercentServiceAreaLedgerAdjuster(-21.9),
            MinimumServiceAreaLedgerAdjuster(11.6),
            RoundingServiceAreaLedgerAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 51593.4,
    )
}
