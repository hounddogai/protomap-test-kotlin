package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global locker batch calculation. */
interface GlobalLockerBatchFactor {
    fun describe(): String
    fun adjust(item: GlobalLockerBatch, value: Double): Double
}

data class RoundingGlobalLockerBatchAdjuster(var step: Double) : GlobalLockerBatchFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalLockerBatch, value: Double): Double = roundTo(value, step)
}

data class CapGlobalLockerBatchAdjuster(var cap: Double) : GlobalLockerBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalLockerBatch, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalLockerBatchAdjuster(var percent: Double) : GlobalLockerBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalLockerBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global locker batch steps in order and keeps a trace of each. */
data class GlobalLockerBatchEngine(
    var steps: MutableList<GlobalLockerBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalLockerBatch): Double {
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

    fun evaluateAll(items: MutableList<GlobalLockerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalLockerBatchEngine(): GlobalLockerBatchEngine {
    return GlobalLockerBatchEngine(
        steps = mutableListOf<GlobalLockerBatchFactor>(
            RoundingGlobalLockerBatchAdjuster(0.5),
            CapGlobalLockerBatchAdjuster(21576.0),
            PercentGlobalLockerBatchAdjuster(1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 63400.9,
    )
}
