package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time allocation calculation. */
interface TransitTimeAllocationFactor {
    fun describe(): String
    fun adjust(item: TransitTimeAllocation, value: Double): Double
}

data class PercentTransitTimeAllocationAdjuster(var percent: Double) : TransitTimeAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTransitTimeAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeAllocationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTransitTimeAllocationAdjuster(var step: Double) : TransitTimeAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double = roundTo(value, step)
}

data class TransitTimeAllocationSequenceOffsetAdjuster(var weight: Double) : TransitTimeAllocationFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class FlaggedTransitTimeAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTransitTimeAllocationAdjuster(var minimum: Double) : TransitTimeAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time allocation steps in order and keeps a trace of each. */
data class TransitTimeAllocationEngine(
    var steps: MutableList<TransitTimeAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeAllocation): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeAllocationEngine(flags: Registry): TransitTimeAllocationEngine {
    return TransitTimeAllocationEngine(
        steps = mutableListOf<TransitTimeAllocationFactor>(
            PercentTransitTimeAllocationAdjuster(2.6),
            NameTransitTimeAllocationAdjuster(mutableMapOf<String, Double>("online" to 1.22, "north" to 0.8)),
            RoundingTransitTimeAllocationAdjuster(0.5),
            TransitTimeAllocationSequenceOffsetAdjuster(1.87),
            FlaggedTransitTimeAllocationAdjuster(flags, "transit-time-allocation-rolling", 2.7),
            MinimumTransitTimeAllocationAdjuster(11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 56990.3,
    )
}
