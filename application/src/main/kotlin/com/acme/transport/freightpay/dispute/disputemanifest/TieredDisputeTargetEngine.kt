package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered dispute target calculation. */
interface TieredDisputeTargetAdjuster {
    fun describe(): String
    fun adjust(item: TieredDisputeTarget, value: Double): Double
}

data class MinimumTieredDisputeTargetAdjuster(var minimum: Double) : TieredDisputeTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDisputeTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredDisputeTargetAdjuster(var cap: Double) : TieredDisputeTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDisputeTarget, value: Double): Double = minOf(value, cap)
}

data class TieredDisputeTargetAttemptsOffsetAdjuster(var weight: Double) : TieredDisputeTargetAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: TieredDisputeTarget, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the tiered dispute target steps in order and keeps a trace of each. */
data class TieredDisputeTargetEngine(
    var steps: MutableList<TieredDisputeTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredDisputeTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDisputeTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredDisputeTargetEngine(): TieredDisputeTargetEngine {
    return TieredDisputeTargetEngine(
        steps = mutableListOf<TieredDisputeTargetAdjuster>(
            MinimumTieredDisputeTargetAdjuster(35.5),
            CapTieredDisputeTargetAdjuster(3292.0),
            TieredDisputeTargetAttemptsOffsetAdjuster(1.18),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 16212.8,
    )
}
